import { createContext, useCallback, useContext, useState } from 'react';
import { AlertTriangle, CheckCircle2, Info, X, XCircle } from 'lucide-react';

const ToastContext = createContext(null);

const TOAST_ICONS = {
  success: CheckCircle2,
  error: XCircle,
  info: Info,
  warning: AlertTriangle,
};

const TOAST_STYLES = {
  success: 'bg-green-50 border-green-200 text-green-800',
  error: 'bg-red-50 border-red-200 text-red-800',
  info: 'bg-blue-50 border-blue-200 text-blue-800',
  warning: 'bg-amber-50 border-amber-200 text-amber-800',
};

const TOAST_ICON_STYLES = {
  success: 'text-green-500',
  error: 'text-red-500',
  info: 'text-blue-500',
  warning: 'text-amber-500',
};

let toastId = 0;

function Toast({ toast, onDismiss }) {
  const Icon = TOAST_ICONS[toast.type] || Info;

  return (
    <div
      role="alert"
      className={`flex items-start gap-3 rounded-lg border px-4 py-3 shadow-lg animate-slide-down ${TOAST_STYLES[toast.type]}`}
    >
      <Icon className={`h-5 w-5 shrink-0 mt-0.5 ${TOAST_ICON_STYLES[toast.type]}`} />
      <div className="flex-1 min-w-0">
        {toast.title && <p className="font-semibold text-sm">{toast.title}</p>}
        {toast.message && (
          <p className={`text-sm ${toast.title ? 'mt-0.5 opacity-90' : ''}`}>{toast.message}</p>
        )}
      </div>
      <button
        onClick={() => onDismiss(toast.id)}
        aria-label="Dismiss notification"
        className="shrink-0 rounded p-0.5 opacity-60 hover:opacity-100 transition-opacity"
      >
        <X className="h-4 w-4" />
      </button>
    </div>
  );
}

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);

  const dismiss = useCallback((id) => {
    setToasts((prev) => prev.filter((t) => t.id !== id));
  }, []);

  const addToast = useCallback(
    ({ type = 'info', title, message, duration = 5000 }) => {
      const id = ++toastId;
      const toast = { id, type, title, message };

      setToasts((prev) => [...prev, toast]);

      if (duration > 0) {
        setTimeout(() => dismiss(id), duration);
      }

      return id;
    },
    [dismiss],
  );

  const toast = useCallback(
    (messageOrOptions) => {
      const options =
        typeof messageOrOptions === 'string'
          ? { message: messageOrOptions }
          : messageOrOptions;
      return addToast(options);
    },
    [addToast],
  );

  toast.success = (message, options) =>
    addToast({ type: 'success', message, ...options });
  toast.error = (message, options) =>
    addToast({ type: 'error', message, ...options });
  toast.info = (message, options) =>
    addToast({ type: 'info', message, ...options });
  toast.warning = (message, options) =>
    addToast({ type: 'warning', message, ...options });

  return (
    <ToastContext.Provider value={toast}>
      {children}
      <div
        aria-live="polite"
        className="fixed top-4 right-4 z-50 flex flex-col gap-2 w-full max-w-sm pointer-events-none"
      >
        {toasts.map((t) => (
          <div key={t.id} className="pointer-events-auto">
            <Toast toast={t} onDismiss={dismiss} />
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const context = useContext(ToastContext);
  if (!context) {
    throw new Error('useToast must be used within a ToastProvider');
  }
  return context;
}
