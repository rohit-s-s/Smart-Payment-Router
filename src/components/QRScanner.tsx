import React, { useState, useRef, useEffect, useCallback } from 'react';
import jsQR from 'jsqr';
import { Camera, Image as ImageIcon, Flashlight, AlertCircle, CheckCircle2, RefreshCw, X, Sparkles, HelpCircle } from 'lucide-react';
import { ParsedUPI, SavedMerchant } from '../types';
import { parseQRCode } from '../utils/upiParser';
import { SAMPLE_QR_CODES, SampleQR } from '../utils/sampleQRs';

interface Props {
  savedMerchants: SavedMerchant[];
  onQRDecoded: (parsed: ParsedUPI) => void;
  onSwitchToManual: (initialData?: Partial<ParsedUPI>) => void;
}

export const QRScanner: React.FC<Props> = ({
  savedMerchants,
  onQRDecoded,
  onSwitchToManual,
}) => {
  const [isScanning, setIsScanning] = useState<boolean>(false);
  const [cameraError, setCameraError] = useState<string | null>(null);
  const [activeFacingMode, setActiveFacingMode] = useState<'environment' | 'user'>('environment');
  const [torchOn, setTorchOn] = useState<boolean>(false);
  const [hasTorch, setHasTorch] = useState<boolean>(false);
  const [statusMessage, setStatusMessage] = useState<string>('Ready to scan or pick from gallery');
  const [dragActive, setDragActive] = useState<boolean>(false);

  const videoRef = useRef<HTMLVideoElement | null>(null);
  const canvasRef = useRef<HTMLCanvasElement | null>(null);
  const streamRef = useRef<MediaStream | null>(null);
  const animationFrameRef = useRef<number | null>(null);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  // Stop camera stream safely
  const stopCamera = useCallback(() => {
    if (animationFrameRef.current) {
      cancelAnimationFrame(animationFrameRef.current);
      animationFrameRef.current = null;
    }
    if (streamRef.current) {
      streamRef.current.getTracks().forEach(track => {
        track.stop();
      });
      streamRef.current = null;
    }
    setIsScanning(false);
    setTorchOn(false);
    setHasTorch(false);
  }, []);

  // Process decoded QR text
  const handleDecodedString = useCallback((decodedText: string) => {
    stopCamera();
    setStatusMessage('QR code detected! Decoding payee details...');

    const parsed = parseQRCode(decodedText, savedMerchants);

    if (parsed.isUPI) {
      onQRDecoded(parsed);
    } else {
      // US-01 bullet 3: If QR is not a UPI string (e.g. a website link), switch to manual entry
      setStatusMessage('Non-UPI QR detected. Switching to manual entry...');
      onSwitchToManual(parsed);
    }
  }, [stopCamera, savedMerchants, onQRDecoded, onSwitchToManual]);

  // Continuous camera frame processing
  const scanFrame = useCallback(() => {
    if (!videoRef.current || !canvasRef.current) return;

    const video = videoRef.current;
    const canvas = canvasRef.current;
    const ctx = canvas.getContext('2d', { willReadFrequently: true });

    if (video.readyState === video.HAVE_ENOUGH_DATA && ctx) {
      canvas.width = video.videoWidth;
      canvas.height = video.videoHeight;
      ctx.drawImage(video, 0, 0, canvas.width, canvas.height);

      const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
      const code = jsQR(imageData.data, imageData.width, imageData.height, {
        inversionAttempts: 'attemptBoth',
      });

      if (code && code.data) {
        handleDecodedString(code.data);
        return; // stop scanning loop
      }
    }

    animationFrameRef.current = requestAnimationFrame(scanFrame);
  }, [handleDecodedString]);

  // Start camera stream
  const startCamera = async () => {
    setCameraError(null);
    setStatusMessage('Requesting camera access...');

    try {
      if (streamRef.current) {
        stopCamera();
      }

      const constraints: MediaStreamConstraints = {
        video: {
          facingMode: activeFacingMode,
          width: { ideal: 1280 },
          height: { ideal: 720 },
        },
      };

      const stream = await navigator.mediaDevices.getUserMedia(constraints);
      streamRef.current = stream;

      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        await videoRef.current.play();
        setIsScanning(true);
        setStatusMessage('Scanning... Align QR code within the frame');

        // Check if torch/flash is supported
        const videoTrack = stream.getVideoTracks()[0];
        const capabilities = (videoTrack.getCapabilities ? videoTrack.getCapabilities() : {}) as { torch?: boolean };
        if (capabilities.torch) {
          setHasTorch(true);
        }

        animationFrameRef.current = requestAnimationFrame(scanFrame);
      }
    } catch (err: unknown) {
      console.warn('Camera error:', err);
      const error = err as Error;
      let msg = 'Could not access device camera. Please check browser permissions.';
      if (error.name === 'NotAllowedError' || error.name === 'PermissionDeniedError') {
        msg = 'Camera permission was denied. Please allow camera access in browser settings.';
      } else if (error.name === 'NotFoundError') {
        msg = 'No camera found on this device. You can pick an image from your gallery instead.';
      }
      setCameraError(msg);
      setIsScanning(false);
      setStatusMessage('Camera unavailable. Pick an image from gallery or use demo QRs.');
    }
  };

  // Toggle torch/flashlight
  const toggleTorch = async () => {
    if (!streamRef.current) return;
    const track = streamRef.current.getVideoTracks()[0];
    if (track && hasTorch) {
      try {
        const newTorchState = !torchOn;
        await (track as unknown as { applyConstraints: (c: unknown) => Promise<void> }).applyConstraints({
          advanced: [{ torch: newTorchState }],
        });
        setTorchOn(newTorchState);
      } catch (err) {
        console.warn('Torch error:', err);
      }
    }
  };

  // Switch between front and rear cameras
  const toggleFacingMode = () => {
    setActiveFacingMode(prev => (prev === 'environment' ? 'user' : 'environment'));
    if (isScanning) {
      setTimeout(() => startCamera(), 100);
    }
  };

  // Clean up camera on unmount
  useEffect(() => {
    return () => {
      stopCamera();
    };
  }, [stopCamera]);

  // Decode QR from uploaded image file / gallery
  const processImageFile = (file: File) => {
    if (!file.type.startsWith('image/')) {
      setCameraError('Please select a valid image file (PNG, JPG, WebP).');
      return;
    }

    setCameraError(null);
    setStatusMessage(`Reading image: ${file.name}...`);

    const reader = new FileReader();
    reader.onload = e => {
      const result = e.target?.result as string;
      if (!result) return;

      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        canvas.width = img.width;
        canvas.height = img.height;
        const ctx = canvas.getContext('2d');
        if (!ctx) {
          setCameraError('Could not process image context.');
          return;
        }

        ctx.drawImage(img, 0, 0);
        const imageData = ctx.getImageData(0, 0, canvas.width, canvas.height);
        const code = jsQR(imageData.data, imageData.width, imageData.height, {
          inversionAttempts: 'attemptBoth',
        });

        if (code && code.data) {
          handleDecodedString(code.data);
        } else {
          setCameraError('No readable QR code found in this image. Try another photo or adjust lighting.');
          setStatusMessage('Scan failed. Select another image or enter manually.');
        }
      };
      img.src = result;
    };
    reader.readAsDataURL(file);
  };

  const handleFileInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const files = e.target.files;
    if (files && files.length > 0) {
      processImageFile(files[0]);
    }
  };

  // Drag and drop handlers
  const handleDragOver = (e: React.DragEvent) => {
    e.preventDefault();
    setDragActive(true);
  };
  const handleDragLeave = () => setDragActive(false);
  const handleDrop = (e: React.DragEvent) => {
    e.preventDefault();
    setDragActive(false);
    if (e.dataTransfer.files && e.dataTransfer.files.length > 0) {
      processImageFile(e.dataTransfer.files[0]);
    }
  };

  return (
    <div 
      className="space-y-6"
      role="region" 
      aria-label="QR Code Scanner for UPI and Merchant Payments"
    >
      {/* Viewfinder & Gallery Card */}
      <div 
        className={`relative overflow-hidden rounded-2xl bg-slate-800/90 border-2 transition-all ${
          dragActive ? 'border-indigo-400 bg-indigo-950/40 ring-4 ring-indigo-500/20' : 'border-slate-700/80 shadow-2xl'
        }`}
        onDragOver={handleDragOver}
        onDragLeave={handleDragLeave}
        onDrop={handleDrop}
      >
        {/* Hidden video and canvas elements */}
        <canvas ref={canvasRef} className="hidden" />

        {isScanning ? (
          <div className="relative aspect-square sm:aspect-video max-h-[380px] w-full bg-black overflow-hidden flex items-center justify-center">
            <video
              ref={videoRef}
              playsInline
              muted
              className="w-full h-full object-cover"
              aria-label="Live camera viewfinder"
            />

            {/* Target Reticle Overlay */}
            <div className="absolute inset-0 pointer-events-none flex items-center justify-center">
              <div className="relative w-64 h-64 sm:w-72 sm:h-72 border-2 border-indigo-400/80 rounded-2xl shadow-[0_0_0_9999px_rgba(15,23,42,0.65)]">
                {/* Corner Accents */}
                <div className="absolute -top-1 -left-1 w-6 h-6 border-t-4 border-l-4 border-indigo-400 rounded-tl-lg" />
                <div className="absolute -top-1 -right-1 w-6 h-6 border-t-4 border-r-4 border-indigo-400 rounded-tr-lg" />
                <div className="absolute -bottom-1 -left-1 w-6 h-6 border-b-4 border-l-4 border-indigo-400 rounded-bl-lg" />
                <div className="absolute -bottom-1 -right-1 w-6 h-6 border-b-4 border-r-4 border-indigo-400 rounded-br-lg" />
                
                {/* Laser scan line animation */}
                <div className="absolute left-2 right-2 h-0.5 bg-gradient-to-r from-transparent via-indigo-400 to-transparent shadow-[0_0_12px_#818cf8] animate-scan-line" />
              </div>
            </div>

            {/* Scanner Controls Floating Toolbar */}
            <div className="absolute top-4 right-4 flex items-center gap-2 z-10">
              {hasTorch && (
                <button
                  type="button"
                  onClick={toggleTorch}
                  aria-label={torchOn ? 'Turn flashlight off' : 'Turn flashlight on'}
                  className={`p-2.5 rounded-full backdrop-blur-md transition ${
                    torchOn ? 'bg-amber-400 text-slate-950 shadow-lg' : 'bg-slate-900/70 text-slate-200 hover:bg-slate-900'
                  }`}
                >
                  <Flashlight className="w-5 h-5" />
                </button>
              )}

              <button
                type="button"
                onClick={toggleFacingMode}
                aria-label="Switch between front and back camera"
                className="p-2.5 rounded-full bg-slate-900/70 hover:bg-slate-900 text-slate-200 backdrop-blur-md transition"
              >
                <RefreshCw className="w-5 h-5" />
              </button>

              <button
                type="button"
                onClick={stopCamera}
                aria-label="Close camera scanner"
                className="p-2.5 rounded-full bg-red-600/80 hover:bg-red-600 text-white backdrop-blur-md transition shadow-md"
              >
                <X className="w-5 h-5" />
              </button>
            </div>
          </div>
        ) : (
          <div className="p-8 sm:p-10 text-center flex flex-col items-center justify-center">
            <div className="w-20 h-20 rounded-2xl bg-indigo-500/10 border border-indigo-500/20 text-indigo-400 flex items-center justify-center mb-4 shadow-inner">
              <Camera className="w-10 h-10" />
            </div>

            <h3 className="text-xl font-bold text-white mb-2">Scan Any UPI or Merchant QR</h3>
            <p className="text-sm text-slate-400 max-w-md mx-auto mb-6">
              Point your camera at a BharatQR, Google Pay, PhonePe, Paytm, or shop counter QR to instantly find your highest-reward card.
            </p>

            {/* Primary Action Buttons */}
            <div className="flex flex-col sm:flex-row items-center justify-center gap-3 w-full max-w-sm">
              <button
                type="button"
                onClick={startCamera}
                aria-label="Open device camera to scan QR"
                className="w-full sm:w-auto flex-1 inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl font-semibold text-white bg-gradient-to-r from-indigo-600 to-indigo-700 hover:from-indigo-500 hover:to-indigo-600 shadow-lg shadow-indigo-600/30 transition-all hover:scale-[1.02] active:scale-[0.98]"
              >
                <Camera className="w-5 h-5" />
                <span>Open Camera</span>
              </button>

              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                aria-label="Upload QR code image from photo gallery"
                className="w-full sm:w-auto flex-1 inline-flex items-center justify-center gap-2 px-6 py-3.5 rounded-xl font-semibold text-slate-200 bg-slate-700/80 hover:bg-slate-700 hover:text-white border border-slate-600 transition-all"
              >
                <ImageIcon className="w-5 h-5" />
                <span>Pick Gallery</span>
              </button>
            </div>

            <p className="text-xs text-slate-500 mt-4">
              Or drag & drop any QR screenshot / image file directly here
            </p>
          </div>
        )}

        {/* Hidden File Input for Gallery Pick */}
        <input
          ref={fileInputRef}
          type="file"
          accept="image/*"
          aria-label="Upload QR image from device gallery"
          className="hidden"
          onChange={handleFileInputChange}
        />

        {/* Live Status Bar */}
        <div 
          className="px-4 py-2.5 bg-slate-900/90 border-t border-slate-700/80 flex items-center justify-between text-xs text-slate-400"
          aria-live="polite"
        >
          <div className="flex items-center gap-2 truncate">
            <span className={`w-2 h-2 rounded-full ${isScanning ? 'bg-emerald-400 animate-ping' : 'bg-slate-500'}`} />
            <span className="truncate">{statusMessage}</span>
          </div>
          <span className="text-[11px] text-slate-500 hidden sm:inline">Offline Ready</span>
        </div>
      </div>

      {/* Camera / Decoding Error Feedback */}
      {cameraError && (
        <div 
          role="alert" 
          className="p-4 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-200 text-sm flex items-start gap-3 animate-fade-in"
        >
          <AlertCircle className="w-5 h-5 text-amber-400 shrink-0 mt-0.5" />
          <div className="flex-1">
            <p className="font-semibold text-amber-300">Scanner Notice</p>
            <p className="text-xs text-amber-200/90 mt-0.5">{cameraError}</p>
          </div>
        </div>
      )}

      {/* Interactive Quick-Test Demo QR Codes (Essential for instantaneous testing without physical codes!) */}
      <div className="p-5 rounded-2xl bg-slate-800/60 border border-slate-700/70">
        <div className="flex items-center justify-between mb-3">
          <div className="flex items-center gap-2">
            <Sparkles className="w-4 h-4 text-indigo-400" />
            <h4 className="text-sm font-semibold text-slate-200">
              Quick Test Scenarios (Epic 1 Verification)
            </h4>
          </div>
          <span className="text-xs text-slate-400">1-Click Test</span>
        </div>

        <p className="text-xs text-slate-400 mb-4">
          Click any preset scenario below to test instant QR decoding, MCC category mapping, amount requirements, and manual fallbacks:
        </p>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
          {SAMPLE_QR_CODES.map((sample: SampleQR) => (
            <button
              key={sample.id}
              type="button"
              onClick={() => handleDecodedString(sample.upiString)}
              className="text-left p-3 rounded-xl bg-slate-900/60 hover:bg-slate-900 border border-slate-700/60 hover:border-indigo-500/50 transition group flex flex-col justify-between"
            >
              <div className="flex items-start justify-between gap-2 mb-1.5">
                <span className="text-xs font-semibold text-slate-200 group-hover:text-indigo-300 transition">
                  {sample.title}
                </span>
                <span className="text-[10px] font-mono px-2 py-0.5 rounded-full bg-indigo-950 text-indigo-300 border border-indigo-800/50 shrink-0">
                  {sample.tag}
                </span>
              </div>
              <p className="text-[11px] text-slate-400 line-clamp-2">
                {sample.expectedOutcome}
              </p>
            </button>
          ))}
        </div>
      </div>
    </div>
  );
};
