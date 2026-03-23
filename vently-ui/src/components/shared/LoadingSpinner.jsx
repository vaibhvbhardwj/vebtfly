export default function LoadingSpinner({ fullPage = false, message = 'Loading...' }) {
  const spinnerContent = (
    <div className="flex flex-col items-center justify-center gap-4">
      <div className="w-12 h-12 border-4 border-gray-200 border-t-blue-600 rounded-full animate-spin" />
      {message && <p className="text-gray-600 text-sm">{message}</p>}
    </div>
  );

  if (fullPage) {
    return (
      <div className="fixed inset-0 bg-white bg-opacity-90 flex items-center justify-center z-50">
        {spinnerContent}
      </div>
    );
  }

  return (
    <div className="flex items-center justify-center py-12">
      {spinnerContent}
    </div>
  );
}
