import { useEffect, useState } from 'react';

const icons = {
  success: { icon: 'bx bx-check-circle', color: 'text-[#10B981]', bg: 'bg-white border-[#10B981]/30' },
  error:   { icon: 'bx bx-x-circle',     color: 'text-[#EF4444]', bg: 'bg-white border-[#EF4444]/30' },
  info:    { icon: 'bx bx-info-circle',  color: 'text-[#807aeb]', bg: 'bg-white border-[#807aeb]/30' },
};

export const Toast = ({ message, type = 'success', onClose, duration = 3500 }) => {
  const [visible, setVisible] = useState(false);

  useEffect(() => {
    // trigger enter animation
    const show = setTimeout(() => setVisible(true), 10);
    const hide = setTimeout(() => { setVisible(false); setTimeout(onClose, 300); }, duration);
    return () => { clearTimeout(show); clearTimeout(hide); };
  }, []);

  const { icon, color, bg } = icons[type] || icons.success;

  return (
    <div className={`flex items-center gap-3 px-4 py-3 rounded-2xl shadow-xl border ${bg} transition-all duration-300 ${visible ? 'opacity-100 translate-y-0' : 'opacity-0 translate-y-4'}`}
      style={{ minWidth: 260, maxWidth: 360 }}>
      <i className={`${icon} text-2xl flex-shrink-0 ${color}`} />
      <p className="text-sm font-medium text-[#111827] flex-1">{message}</p>
      <button onClick={() => { setVisible(false); setTimeout(onClose, 300); }}
        className="text-[#9CA3AF] hover:text-[#111827] transition text-lg leading-none flex-shrink-0">✕</button>
    </div>
  );
};

// Container rendered once in App
let _addToast = null;
export const setToastHandler = (fn) => { _addToast = fn; };
export const toast = {
  success: (msg) => _addToast?.({ message: msg, type: 'success' }),
  error:   (msg) => _addToast?.({ message: msg, type: 'error' }),
  info:    (msg) => _addToast?.({ message: msg, type: 'info' }),
};

export const ToastContainer = () => {
  const [toasts, setToasts] = useState([]);

  useEffect(() => {
    setToastHandler((t) => setToasts(prev => [...prev, { ...t, id: Date.now() + Math.random() }]));
    return () => setToastHandler(null);
  }, []);

  const remove = (id) => setToasts(prev => prev.filter(t => t.id !== id));

  return (
    <div className="fixed bottom-6 left-1/2 -translate-x-1/2 z-[9999] flex flex-col gap-2 items-center pointer-events-none">
      {toasts.map(t => (
        <div key={t.id} className="pointer-events-auto">
          <Toast message={t.message} type={t.type} onClose={() => remove(t.id)} />
        </div>
      ))}
    </div>
  );
};
