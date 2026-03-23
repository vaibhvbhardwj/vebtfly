export default function LoadingSkeleton({ count = 3, type = 'card' }) {
  const skeletonCard = (
    <div className="bg-white rounded-lg shadow p-4 animate-pulse">
      <div className="h-48 bg-gray-200 rounded mb-4" />
      <div className="h-4 bg-gray-200 rounded mb-2" />
      <div className="h-4 bg-gray-200 rounded mb-2 w-5/6" />
      <div className="h-4 bg-gray-200 rounded w-4/6" />
    </div>
  );

  const skeletonListItem = (
    <div className="bg-white rounded-lg shadow p-4 animate-pulse flex gap-4">
      <div className="w-16 h-16 bg-gray-200 rounded-full flex-shrink-0" />
      <div className="flex-1">
        <div className="h-4 bg-gray-200 rounded mb-2 w-1/3" />
        <div className="h-4 bg-gray-200 rounded mb-2" />
        <div className="h-4 bg-gray-200 rounded w-2/3" />
      </div>
    </div>
  );

  const skeletonTable = (
    <div className="bg-white rounded-lg shadow overflow-hidden animate-pulse">
      <div className="p-4 border-b border-gray-200 flex gap-4">
        <div className="h-4 bg-gray-200 rounded flex-1" />
        <div className="h-4 bg-gray-200 rounded flex-1" />
        <div className="h-4 bg-gray-200 rounded flex-1" />
      </div>
      {Array.from({ length: count }).map((_, i) => (
        <div key={i} className="p-4 border-b border-gray-200 flex gap-4">
          <div className="h-4 bg-gray-200 rounded flex-1" />
          <div className="h-4 bg-gray-200 rounded flex-1" />
          <div className="h-4 bg-gray-200 rounded flex-1" />
        </div>
      ))}
    </div>
  );

  const getSkeletonType = () => {
    switch (type) {
      case 'list':
        return skeletonListItem;
      case 'table':
        return skeletonTable;
      case 'card':
      default:
        return skeletonCard;
    }
  };

  if (type === 'table') {
    return skeletonTable;
  }

  return (
    <div className={type === 'list' ? 'space-y-4' : 'grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4'}>
      {Array.from({ length: count }).map((_, i) => (
        <div key={i}>{getSkeletonType()}</div>
      ))}
    </div>
  );
}
