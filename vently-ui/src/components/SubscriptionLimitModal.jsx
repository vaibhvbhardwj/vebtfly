import { Link } from 'react-router-dom';

const SubscriptionLimitModal = ({ isOpen, onClose, type = 'events', currentCount = 3, limit = 3 }) => {
  if (!isOpen) return null;

  const messages = {
    events: {
      title: 'Event Limit Reached',
      description: `You've reached the limit of ${limit} events per month on the Free plan.`,
    },
    applications: {
      title: 'Application Limit Reached',
      description: `You've reached the limit of ${limit} applications per month on the Free plan.`,
    },
  };

  const message = messages[type] || messages.events;

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center p-4 z-50">
      <div className="bg-white rounded-2xl border border-[#807aeb]/10 max-w-md w-full p-8 shadow-xl animate-slide-up">
        {/* Icon */}
        <div className="text-center mb-4">
          <div className="inline-flex items-center justify-center w-16 h-16 bg-yellow-100 rounded-full mb-4">
            <svg className="w-8 h-8 text-yellow-600" fill="currentColor" viewBox="0 0 20 20">
              <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
            </svg>
          </div>
        </div>

        <h2 className="text-2xl font-bold text-[#111827] mb-3 text-center">{message.title}</h2>
        <p className="text-[#6B7280] text-center mb-6">{message.description}</p>

        {/* Usage bar */}
        <div className="mb-6 p-4 bg-[#ebf2fa] rounded-xl border border-[#807aeb]/10">
          <div className="flex justify-between items-center mb-2">
            <span className="text-sm font-medium text-[#111827]">Current Usage</span>
            <span className="text-sm font-bold text-[#111827]">{currentCount}/{limit}</span>
          </div>
          <div className="w-full bg-white rounded-full h-2 border border-[#807aeb]/10">
            <div
              className="bg-[#807aeb] h-2 rounded-full transition-all"
              style={{ width: `${Math.min((currentCount / limit) * 100, 100)}%` }}
            />
          </div>
        </div>

        {/* Benefits */}
        <div className="mb-6 p-4 bg-[#807aeb]/5 rounded-xl border border-[#807aeb]/10">
          <p className="text-sm text-[#111827] mb-3 font-medium">Premium Plan includes:</p>
          <ul className="space-y-2 text-sm text-[#6B7280]">
            {[`Unlimited ${type}`, 'Priority support', 'Verification badge'].map(item => (
              <li key={item} className="flex items-center gap-2">
                <svg className="w-4 h-4 text-[#10B981] flex-shrink-0" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
                {item}
              </li>
            ))}
          </ul>
        </div>

        {/* Buttons */}
        <div className="flex gap-3">
          <button onClick={onClose}
            className="flex-1 py-2 px-4 bg-[#ebf2fa] text-[#6B7280] rounded-xl font-medium hover:bg-[#807aeb]/10 transition">
            Maybe Later
          </button>
          <Link to="/subscription"
            className="flex-1 py-2 px-4 bg-[#807aeb] text-white rounded-xl font-medium hover:bg-[#6c66d4] transition text-center">
            Upgrade Now
          </Link>
        </div>
      </div>
    </div>
  );
};

export default SubscriptionLimitModal;
