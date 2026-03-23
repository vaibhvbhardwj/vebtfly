import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../hooks/useAuth';

const FloatingDashboardButton = () => {
  const navigate = useNavigate();
  const { user, isAuthenticated } = useAuth();
  const [position, setPosition] = useState({ x: 0, y: 0 });
  const [isDragging, setIsDragging] = useState(false);
  const [dragOffset, setDragOffset] = useState({ x: 0, y: 0 });
  const [isMobile, setIsMobile] = useState(window.innerWidth < 768);
  const buttonRef = useRef(null);

  useEffect(() => {
    const handleResize = () => setIsMobile(window.innerWidth < 768);
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  useEffect(() => {
    setPosition({ x: window.innerWidth - 80, y: window.innerHeight - 170 });
  }, []);

  const handleMouseDown = (e) => {
    setIsDragging(true);
    const rect = buttonRef.current.getBoundingClientRect();
    setDragOffset({ x: e.clientX - rect.left, y: e.clientY - rect.top });
  };

  const handleMouseMove = (e) => {
    if (!isDragging) return;
    const newX = e.clientX - dragOffset.x;
    const newY = e.clientY - dragOffset.y;
    setPosition({
      x: Math.max(0, Math.min(newX, window.innerWidth - 70)),
      y: Math.max(0, Math.min(newY, window.innerHeight - 70)),
    });
  };

  const handleMouseUp = () => setIsDragging(false);

  useEffect(() => {
    if (isDragging) {
      document.addEventListener('mousemove', handleMouseMove);
      document.addEventListener('mouseup', handleMouseUp);
      return () => {
        document.removeEventListener('mousemove', handleMouseMove);
        document.removeEventListener('mouseup', handleMouseUp);
      };
    }
  }, [isDragging, dragOffset]);

  const handleDashboardClick = () => {
    if (!isDragging) {
      if (user?.role === 'ORGANIZER') navigate('/organizer/dashboard');
      else if (user?.role === 'VOLUNTEER') navigate('/volunteer/dashboard');
      else if (user?.role === 'ADMIN') navigate('/admin/dashboard');
    }
  };

  if (!isAuthenticated || !user) return null;

  return (
    <div
      ref={buttonRef}
      onMouseDown={handleMouseDown}
      onClick={handleDashboardClick}
      style={{
        position: 'fixed',
        left: `${position.x}px`,
        top: `${position.y}px`,
        zIndex: 40,
        cursor: isDragging ? 'grabbing' : 'grab',
      }}
    >
      <button
        className="w-16 h-16 bg-[#807aeb] text-white rounded-full shadow-2xl flex items-center justify-center hover:bg-[#6c66d4] hover:scale-110 transition-all duration-200 active:scale-95"
        title="Go to Dashboard"
        style={{ pointerEvents: isDragging ? 'none' : 'auto' }}
      >
        <i className="bx bxs-dashboard text-3xl" />
      </button>
      <div className="absolute bottom-full right-0 mb-2 bg-[#111827]/80 text-white text-xs px-2 py-1 rounded-lg whitespace-nowrap pointer-events-none opacity-0 hover:opacity-100 transition-opacity">
        Dashboard
      </div>
    </div>
  );
};

export default FloatingDashboardButton;
