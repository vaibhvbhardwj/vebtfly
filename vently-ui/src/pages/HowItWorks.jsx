import React from 'react';
import { Link } from 'react-router-dom';

const HowItWorks = () => {
  return (
    <div className="min-h-screen bg-[#ebf2fa] animate-fade-in">
      <div className="relative max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-16">
        <h1 className="text-4xl font-bold text-[#111827] mb-4">How Vently Works</h1>
        <p className="text-xl text-[#6B7280] mb-12">Connect volunteers with opportunities</p>

        <div className="grid md:grid-cols-2 gap-8 mb-12">
          {/* Volunteer Section */}
          <div className="bg-white border border-[#807aeb]/10 rounded-2xl p-8 shadow-sm card-hover animate-slide-up">
            <h2 className="text-2xl font-bold text-[#807aeb] mb-6">For Volunteers</h2>
            <div className="space-y-4">
              {[
                { n: 1, title: 'Sign Up', desc: 'Create your account and complete your profile' },
                { n: 2, title: 'Browse Events', desc: 'Find volunteer opportunities that match your interests' },
                { n: 3, title: 'Apply', desc: 'Submit your application to events you\'re interested in' },
                { n: 4, title: 'Get Accepted & Attend', desc: 'Confirm your attendance and participate in the event' },
                { n: 5, title: 'Get Paid & Rate', desc: 'Receive payment and rate your experience' },
              ].map(({ n, title, desc }) => (
                <div key={n} className="flex gap-4">
                  <div className="flex-shrink-0 w-8 h-8 bg-[#807aeb] rounded-full flex items-center justify-center text-white font-bold text-sm">{n}</div>
                  <div>
                    <h3 className="font-semibold text-[#111827]">{title}</h3>
                    <p className="text-[#6B7280] text-sm">{desc}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {/* Organizer Section */}
          <div className="bg-white border border-[#807aeb]/10 rounded-2xl p-8 shadow-sm card-hover animate-slide-up" style={{ animationDelay: '0.1s' }}>
            <h2 className="text-2xl font-bold text-[#10B981] mb-6">For Organizers</h2>
            <div className="space-y-4">
              {[
                { n: 1, title: 'Sign Up', desc: 'Create your account as an organizer' },
                { n: 2, title: 'Post Event', desc: 'Create and publish your volunteer event' },
                { n: 3, title: 'Review Applications', desc: 'Accept or reject volunteer applications' },
                { n: 4, title: 'Pay & Manage', desc: 'Deposit payment and manage attendance' },
                { n: 5, title: 'Rate Volunteers', desc: 'Rate and review volunteer performance' },
              ].map(({ n, title, desc }) => (
                <div key={n} className="flex gap-4">
                  <div className="flex-shrink-0 w-8 h-8 bg-[#10B981] rounded-full flex items-center justify-center text-white font-bold text-sm">{n}</div>
                  <div>
                    <h3 className="font-semibold text-[#111827]">{title}</h3>
                    <p className="text-[#6B7280] text-sm">{desc}</p>
                  </div>
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* CTA Section */}
        <div className="bg-[#807aeb] rounded-2xl p-8 text-center shadow-sm">
          <h2 className="text-2xl font-bold text-white mb-4">Ready to Get Started?</h2>
          <p className="text-white/80 mb-6">Join thousands of volunteers and organizers on Vently</p>
          <Link
            to="/register"
            className="inline-block px-8 py-3 bg-white text-[#807aeb] font-semibold rounded-xl hover:bg-[#ebf2fa] transition"
          >
            Sign Up Now
          </Link>
        </div>
      </div>
    </div>
  );
};

export default HowItWorks;
