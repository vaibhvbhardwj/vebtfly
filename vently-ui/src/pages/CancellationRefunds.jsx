const CancellationRefunds = () => {
  return (
    <div className="min-h-screen bg-[#ebf2fa] py-12">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold text-[#111827] mb-4">Cancellation & Refunds Policy</h1>
          <p className="text-lg text-[#6B7280]">
            Last updated: March 21, 2026
          </p>
        </div>

        <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-8 lg:p-12">
          <div className="prose prose-lg max-w-none">
            
            {/* Overview */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">1. Overview</h2>
              <p className="text-[#6B7280] mb-4">
                At Vently, we strive to provide the best experience for both organizers and volunteers. This policy outlines our cancellation and refund procedures for subscriptions, event payments, and other transactions on our platform.
              </p>
              <p className="text-[#6B7280] mb-4">
                We understand that circumstances can change, and we've designed our policies to be fair and transparent for all users.
              </p>
            </section>

            {/* Subscription Cancellations */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">2. Subscription Cancellations & Refunds</h2>
              
              <h3 className="text-xl font-semibold text-[#111827] mb-3">2.1 Premium Subscription Cancellation</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li><strong>Anytime Cancellation:</strong> You can cancel your Premium subscription at any time from your account settings</li>
                <li><strong>Access Until Period End:</strong> You'll retain Premium features until the end of your current billing period</li>
                <li><strong>No Auto-Renewal:</strong> Your subscription will not renew after cancellation</li>
                <li><strong>Downgrade to Free:</strong> Your account will automatically downgrade to the Free plan</li>
              </ul>

              <h3 className="text-xl font-semibold text-[#111827] mb-3">2.2 Subscription Refunds</h3>
              <div className="bg-[#10B981]/5 border border-[#10B981]/20 rounded-xl p-6 mb-4">
                <h4 className="font-semibold text-[#111827] mb-2">
                  <i className="bx bx-check-circle text-[#10B981] mr-2" />
                  7-Day Money-Back Guarantee
                </h4>
                <ul className="list-disc pl-6 space-y-1 text-[#6B7280] text-sm">
                  <li>Full refund if cancelled within 7 days of first subscription</li>
                  <li>Applies only to first-time Premium subscribers</li>
                  <li>Refund processed within 5-7 business days</li>
                </ul>
              </div>

              <h4 className="text-lg font-semibold text-[#111827] mb-3">Partial Refunds</h4>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li><strong>Pro-rated refunds</strong> may be considered for exceptional circumstances</li>
                <li>Technical issues preventing service use for extended periods</li>
                <li>Platform downtime exceeding 48 hours in a billing period</li>
                <li>Each case is reviewed individually by our support team</li>
              </ul>

              <h4 className="text-lg font-semibold text-[#111827] mb-3">No Refund Scenarios</h4>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Cancellations after the 7-day guarantee period</li>
                <li>Account suspension due to Terms of Service violations</li>
                <li>Change of mind or unused features</li>
                <li>Renewal charges (unless cancelled before renewal date)</li>
              </ul>
            </section>

            {/* Event Payment Cancellations */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">3. Event Payment Cancellations</h2>
              
              <h3 className="text-xl font-semibold text-[#111827] mb-3">3.1 For Organizers</h3>
              
              <h4 className="text-lg font-semibold text-[#111827] mb-3">Event Cancellation by Organizer</h4>
              <div className="space-y-4 mb-6">
                <div className="bg-[#10B981]/5 border border-[#10B981]/20 rounded-xl p-4">
                  <h5 className="font-semibold text-[#111827] mb-2">
                    <i className="bx bx-time text-[#10B981] mr-2" />
                    More than 48 hours before event
                  </h5>
                  <ul className="list-disc pl-6 space-y-1 text-[#6B7280] text-sm">
                    <li>Full refund of deposit paid</li>
                    <li>Platform fee refunded</li>
                    <li>Volunteers notified automatically</li>
                  </ul>
                </div>
                
                <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-4">
                  <h5 className="font-semibold text-[#111827] mb-2">
                    <i className="bx bx-error text-yellow-500 mr-2" />
                    24-48 hours before event
                  </h5>
                  <ul className="list-disc pl-6 space-y-1 text-[#6B7280] text-sm">
                    <li>50% refund of deposit paid</li>
                    <li>Platform fee non-refundable</li>
                    <li>Volunteers compensated for short notice</li>
                  </ul>
                </div>
                
                <div className="bg-red-50 border border-red-200 rounded-xl p-4">
                  <h5 className="font-semibold text-[#111827] mb-2">
                    <i className="bx bx-x-circle text-[#EF4444] mr-2" />
                    Less than 24 hours before event
                  </h5>
                  <ul className="list-disc pl-6 space-y-1 text-[#6B7280] text-sm">
                    <li>No refund of deposit</li>
                    <li>Platform fee non-refundable</li>
                    <li>Volunteers receive full compensation</li>
                    <li>Account may be flagged for repeated violations</li>
                  </ul>
                </div>
              </div>

              <h3 className="text-xl font-semibold text-[#111827] mb-3">3.2 For Volunteers</h3>
              
              <h4 className="text-lg font-semibold text-[#111827] mb-3">Volunteer Withdrawal</h4>
              <div className="space-y-4 mb-6">
                <div className="bg-[#10B981]/5 border border-[#10B981]/20 rounded-xl p-4">
                  <h5 className="font-semibold text-[#111827] mb-2">
                    <i className="bx bx-time text-[#10B981] mr-2" />
                    More than 48 hours before event
                  </h5>
                  <ul className="list-disc pl-6 space-y-1 text-[#6B7280] text-sm">
                    <li>No penalty for withdrawal</li>
                    <li>Application slot released for others</li>
                    <li>No impact on account standing</li>
                  </ul>
                </div>
                
                <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-4">
                  <h5 className="font-semibold text-[#111827] mb-2">
                    <i className="bx bx-error text-yellow-500 mr-2" />
                    24-48 hours before event
                  </h5>
                  <ul className="list-disc pl-6 space-y-1 text-[#6B7280] text-sm">
                    <li>Warning issued to account</li>
                    <li>Organizer notified for replacement</li>
                    <li>May affect future application priority</li>
                  </ul>
                </div>
                
                <div className="bg-red-50 border border-red-200 rounded-xl p-4">
                  <h5 className="font-semibold text-[#111827] mb-2">
                    <i className="bx bx-x-circle text-[#EF4444] mr-2" />
                    No-show (without notice)
                  </h5>
                  <ul className="list-disc pl-6 space-y-1 text-[#6B7280] text-sm">
                    <li>No-show count increased</li>
                    <li>Account may be suspended after 3 no-shows</li>
                    <li>Reduced priority in future applications</li>
                    <li>Organizer may leave negative feedback</li>
                  </ul>
                </div>
              </div>
            </section>

            {/* Refund Process */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">4. Refund Process</h2>
              
              <h3 className="text-xl font-semibold text-[#111827] mb-3">4.1 How to Request a Refund</h3>
              <div className="bg-[#807aeb]/5 border border-[#807aeb]/20 rounded-xl p-6 mb-4">
                <ol className="list-decimal pl-6 space-y-2 text-[#6B7280]">
                  <li>Log into your Vently account</li>
                  <li>Go to "Billing" or "Subscription" section</li>
                  <li>Click "Request Refund" or contact support</li>
                  <li>Provide reason for refund request</li>
                  <li>Submit supporting documentation if applicable</li>
                </ol>
              </div>

              <h3 className="text-xl font-semibold text-[#111827] mb-3">4.2 Refund Timeline</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li><strong>Review Period:</strong> 2-3 business days for request review</li>
                <li><strong>Processing Time:</strong> 5-7 business days after approval</li>
                <li><strong>Bank Transfer:</strong> Additional 2-3 days depending on your bank</li>
                <li><strong>UPI Refunds:</strong> Usually instant to 24 hours</li>
              </ul>

              <h3 className="text-xl font-semibold text-[#111827] mb-3">4.3 Refund Methods</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Refunds are processed to the original payment method</li>
                <li>UPI payments refunded to the same UPI ID</li>
                <li>Card payments refunded to the same card</li>
                <li>Alternative methods may be arranged in special cases</li>
              </ul>
            </section>

            {/* Dispute Resolution */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">5. Dispute Resolution</h2>
              
              <h3 className="text-xl font-semibold text-[#111827] mb-3">5.1 Event Disputes</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Disputes between organizers and volunteers are mediated by Vently</li>
                <li>Evidence from both parties is reviewed</li>
                <li>Platform may hold payments pending resolution</li>
                <li>Final decisions are made within 7 business days</li>
              </ul>

              <h3 className="text-xl font-semibold text-[#111827] mb-3">5.2 Payment Disputes</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Contact our support team immediately</li>
                <li>Provide transaction details and evidence</li>
                <li>We work with payment processors to resolve issues</li>
                <li>Chargebacks are handled according to bank policies</li>
              </ul>
            </section>

            {/* Special Circumstances */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">6. Special Circumstances</h2>
              
              <h3 className="text-xl font-semibold text-[#111827] mb-3">6.1 Force Majeure Events</h3>
              <p className="text-[#6B7280] mb-4">
                In case of natural disasters, government restrictions, or other unforeseeable events:
              </p>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Full refunds provided for cancelled events</li>
                <li>Subscription extensions may be offered</li>
                <li>No penalties for cancellations due to force majeure</li>
                <li>Alternative arrangements made when possible</li>
              </ul>

              <h3 className="text-xl font-semibold text-[#111827] mb-3">6.2 Technical Issues</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Platform downtime exceeding 4 hours may qualify for refunds</li>
                <li>Payment processing errors are resolved immediately</li>
                <li>Data loss incidents handled with priority support</li>
                <li>Service credits may be provided for extended outages</li>
              </ul>
            </section>

            {/* Contact Information */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">7. Contact for Refunds</h2>
              <p className="text-[#6B7280] mb-4">
                For any refund requests or questions about this policy, please contact us:
              </p>
              <div className="bg-[#ebf2fa] rounded-xl p-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div>
                    <h4 className="font-semibold text-[#111827] mb-2">Support Team</h4>
                    <p className="text-[#6B7280] text-sm mb-1">Email: refunds@ventfly.com</p>
                    <p className="text-[#6B7280] text-sm mb-1">Phone: +91 83688 01490</p>
                    <p className="text-[#6B7280] text-sm">Hours: 9 AM - 6 PM IST (Mon-Fri)</p>
                  </div>
                  <div>
                    <h4 className="font-semibold text-[#111827] mb-2">Response Time</h4>
                    <p className="text-[#6B7280] text-sm mb-1">Email: Within 24 hours</p>
                    <p className="text-[#6B7280] text-sm mb-1">Phone: Immediate during business hours</p>
                    <p className="text-[#6B7280] text-sm">Urgent issues: Within 4 hours</p>
                  </div>
                </div>
              </div>
            </section>

            {/* Policy Updates */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">8. Policy Updates</h2>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>This policy may be updated from time to time</li>
                <li>Users will be notified of material changes</li>
                <li>Changes take effect 30 days after notification</li>
                <li>Continued use implies acceptance of updated terms</li>
              </ul>
            </section>

            {/* Fair Usage */}
            <section className="mb-8">
              <div className="bg-[#807aeb]/5 border border-[#807aeb]/20 rounded-xl p-6">
                <h3 className="text-lg font-semibold text-[#111827] mb-3">
                  <i className="bx bx-shield-check text-[#807aeb] mr-2" />
                  Our Commitment to Fairness
                </h3>
                <p className="text-[#6B7280] text-sm mb-3">
                  We believe in treating all users fairly and transparently. Our refund policy is designed to protect both organizers and volunteers while maintaining the integrity of our platform.
                </p>
                <p className="text-[#6B7280] text-sm">
                  If you feel a decision was unfair or have special circumstances not covered by this policy, please reach out to our support team. We review each case individually and strive to find reasonable solutions.
                </p>
              </div>
            </section>
          </div>
        </div>
      </div>
    </div>
  );
};

export default CancellationRefunds;