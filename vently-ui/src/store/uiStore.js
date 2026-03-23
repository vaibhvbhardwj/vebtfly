import { create } from 'zustand';

export const useUIStore = create((set) => ({
  loading: false,
  modals: {
    confirmDialog: { isOpen: false, title: '', message: '', onConfirm: null },
    errorDialog: { isOpen: false, title: '', message: '' },
    successDialog: { isOpen: false, title: '', message: '' },
  },
  sidebarOpen: false,
  toast: { isOpen: false, message: '', type: 'info' }, // info, success, error, warning

  // Set loading state
  setLoading: (loading) => {
    set({ loading });
  },

  // Open confirm dialog
  openConfirmDialog: (title, message, onConfirm) => {
    set({
      modals: {
        ...set((state) => state.modals),
        confirmDialog: { isOpen: true, title, message, onConfirm },
      },
    });
  },

  // Close confirm dialog
  closeConfirmDialog: () => {
    set((state) => ({
      modals: {
        ...state.modals,
        confirmDialog: { isOpen: false, title: '', message: '', onConfirm: null },
      },
    }));
  },

  // Open error dialog
  openErrorDialog: (title, message) => {
    set((state) => ({
      modals: {
        ...state.modals,
        errorDialog: { isOpen: true, title, message },
      },
    }));
  },

  // Close error dialog
  closeErrorDialog: () => {
    set((state) => ({
      modals: {
        ...state.modals,
        errorDialog: { isOpen: false, title: '', message: '' },
      },
    }));
  },

  // Open success dialog
  openSuccessDialog: (title, message) => {
    set((state) => ({
      modals: {
        ...state.modals,
        successDialog: { isOpen: true, title, message },
      },
    }));
  },

  // Close success dialog
  closeSuccessDialog: () => {
    set((state) => ({
      modals: {
        ...state.modals,
        successDialog: { isOpen: false, title: '', message: '' },
      },
    }));
  },

  // Toggle sidebar
  toggleSidebar: () => {
    set((state) => ({
      sidebarOpen: !state.sidebarOpen,
    }));
  },

  // Open sidebar
  openSidebar: () => {
    set({ sidebarOpen: true });
  },

  // Close sidebar
  closeSidebar: () => {
    set({ sidebarOpen: false });
  },

  // Show toast
  showToast: (message, type = 'info') => {
    set({ toast: { isOpen: true, message, type } });
    setTimeout(() => {
      set({ toast: { isOpen: false, message: '', type: 'info' } });
    }, 3000);
  },

  // Hide toast
  hideToast: () => {
    set({ toast: { isOpen: false, message: '', type: 'info' } });
  },
}));
