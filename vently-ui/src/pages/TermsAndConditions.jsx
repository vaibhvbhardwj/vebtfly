const TermsAndConditions = () => {
  return (
    <div className="min-h-screen bg-[#ebf2fa] py-12">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold text-[#111827] mb-4">Terms and Conditions</h1>
          <p className="text-lg text-[#6B7280]">
            Last updated: March 21, 2026
          </p>
        </div>

        <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-8 lg:p-12">
          <div className="prose prose-lg max-w-none">
            
            {/* Introduction */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">1. Introduction</h2>
              <p className="text-[#6B7280] mb-4">
                Welcome to Vently ("we," "our," or "us"). These Terms and Conditions ("Terms") govern your use of the Vently platform, website, and mobile application (collectively, the "Service") operated by Vently Technologies Pvt. Ltd.
              </p>
              <p className="text-[#6B7280] mb-4">
                By accessing or using our Service, you agree to be bound by these Terms. If you disagree with any part of these terms, then you may not access the Service.
              </p>
            </section>

            {/* Definitions */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">2. Definitions</h2>
              <div className="space-y-3">
                <p className="text-[#6B7280]">
                  <strong>"Platform"</strong> refers to the Vently website and mobile application.
                </p>
                <p className="text-[#6B7280]">
                  <strong>"User"</strong> refers to any individual who accesses or uses the Service.
                </p>
                <p className="text-[#6B7280]">
                  <strong>"Organizer"</strong> refers to users who create and manage volunteer events.
                </p>
                <p className="text-[#6B7280]">
                  <strong>"Volunteer"</strong> refers to users who apply for and participate in events.
                </p>
                <p className="text-[#6B7280]">
                  <strong>"Event"</strong> refers to volunteer opportunities posted on the Platform.
                </p>
              </div>
            </section>

            {/* User Accounts */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">3. User Accounts</h2>
              <h3 className="text-xl font-semibold text-[#111827] mb-3">3.1 Account Creation</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>You must be at least 18 years old to create an account</li>
                <li>You must provide accurate and complete information</li>
                <li>You are responsible for maintaining the security of your account</li>
                <li>Phone number verification is mandatory for all users</li>
                <li>One phone number can only be linked to one account</li>
              </ul>
              
              <h3 className="text-xl font-semibold text-[#111827] mb-3">3.2 Account Responsibilities</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>You are responsible for all activities under your account</li>
                <li>You must notify us immediately of any unauthorized use</li>
                <li>You must keep your contact information up to date</li>
                <li>You may not share your account credentials with others</li>
              </ul>
            </section>

            {/* Platform Usage */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">4. Platform Usage</h2>
              <h3 className="text-xl font-semibold text-[#111827] mb-3">4.1 For Organizers</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Free accounts are limited to 3 active events</li>
                <li>Premium accounts have unlimited events</li>
                <li>You must provide accurate event information</li>
                <li>You are responsible for volunteer payments and management</li>
                <li>You must comply with all applicable laws and regulations</li>
              </ul>
              
              <h3 className="text-xl font-semibold text-[#111827] mb-3">4.2 For Volunteers</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Free accounts are limited to 5 active applications</li>
                <li>Premium accounts have unlimited applications</li>
                <li>You must attend confirmed events or notify in advance</li>
                <li>Excessive no-shows may result in account suspension</li>
                <li>You must provide accurate information in applications</li>
              </ul>
            </section>

            {/* Payments and Subscriptions */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">5. Payments and Subscriptions</h2>
              <h3 className="text-xl font-semibold text-[#111827] mb-3">5.1 Subscription Plans</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Free plan with limited features</li>
                <li>Premium plan at ₹499/month with unlimited features</li>
                <li>Subscriptions auto-renew unless cancelled</li>
                <li>Prices may change with 30 days notice</li>
              </ul>
              
              <h3 className="text-xl font-semibold text-[#111827] mb-3">5.2 Event Payments</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Organizers pay volunteers directly through the platform</li>
                <li>Platform fee of 5% applies to all transactions</li>
                <li>Payments are processed securely via Razorpay</li>
                <li>Refunds are subject to our refund policy</li>
              </ul>
            </section>

            {/* Prohibited Activities */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">6. Prohibited Activities</h2>
              <p className="text-[#6B7280] mb-4">You may not:</p>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Use the platform for illegal activities</li>
                <li>Post false or misleading information</li>
                <li>Harass, abuse, or harm other users</li>
                <li>Attempt to circumvent platform fees</li>
                <li>Create multiple accounts to bypass limits</li>
                <li>Use automated tools to access the platform</li>
                <li>Reverse engineer or copy the platform</li>
                <li>Post content that violates intellectual property rights</li>
              </ul>
            </section>

            {/* Content and Intellectual Property */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">7. Content and Intellectual Property</h2>
              <h3 className="text-xl font-semibold text-[#111827] mb-3">7.1 User Content</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>You retain ownership of content you post</li>
                <li>You grant us license to use your content on the platform</li>
                <li>You are responsible for ensuring you have rights to posted content</li>
                <li>We may remove content that violates these Terms</li>
              </ul>
              
              <h3 className="text-xl font-semibold text-[#111827] mb-3">7.2 Platform Content</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>All platform features and design are our intellectual property</li>
                <li>You may not copy, modify, or distribute our content</li>
                <li>Trademarks and logos are protected</li>
              </ul>
            </section>

            {/* Privacy and Data */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">8. Privacy and Data Protection</h2>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Your privacy is important to us</li>
                <li>Please review our Privacy Policy for details on data handling</li>
                <li>We comply with applicable data protection laws</li>
                <li>You can request data deletion at any time</li>
              </ul>
            </section>

            {/* Termination */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">9. Account Termination</h2>
              <h3 className="text-xl font-semibold text-[#111827] mb-3">9.1 By You</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>You may delete your account at any time</li>
                <li>Subscription cancellations take effect at the end of the billing period</li>
                <li>Some data may be retained for legal compliance</li>
              </ul>
              
              <h3 className="text-xl font-semibold text-[#111827] mb-3">9.2 By Us</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>We may suspend or terminate accounts for Terms violations</li>
                <li>We may terminate the service with 30 days notice</li>
                <li>Immediate termination for serious violations</li>
              </ul>
            </section>

            {/* Disclaimers */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">10. Disclaimers and Limitations</h2>
              <h3 className="text-xl font-semibold text-[#111827] mb-3">10.1 Service Availability</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>We strive for 99.9% uptime but cannot guarantee uninterrupted service</li>
                <li>Maintenance windows may cause temporary unavailability</li>
                <li>We are not liable for service interruptions</li>
              </ul>
              
              <h3 className="text-xl font-semibold text-[#111827] mb-3">10.2 User Interactions</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>We facilitate connections but are not party to agreements between users</li>
                <li>We are not responsible for user conduct or disputes</li>
                <li>Users interact at their own risk</li>
              </ul>
            </section>

            {/* Liability */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">11. Limitation of Liability</h2>
              <p className="text-[#6B7280] mb-4">
                To the maximum extent permitted by law, Vently shall not be liable for any indirect, incidental, special, consequential, or punitive damages, including but not limited to loss of profits, data, or use, arising out of or relating to your use of the Service.
              </p>
              <p className="text-[#6B7280] mb-4">
                Our total liability to you for all claims shall not exceed the amount you paid us in the 12 months preceding the claim.
              </p>
            </section>

            {/* Governing Law */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">12. Governing Law and Disputes</h2>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>These Terms are governed by the laws of India</li>
                <li>Disputes will be resolved in the courts of Gurugram, Haryana</li>
                <li>We encourage resolving disputes through our support team first</li>
                <li>Arbitration may be required for certain disputes</li>
              </ul>
            </section>

            {/* Changes to Terms */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">13. Changes to Terms</h2>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>We may update these Terms from time to time</li>
                <li>Material changes will be notified via email or platform notification</li>
                <li>Continued use after changes constitutes acceptance</li>
                <li>You should review Terms periodically</li>
              </ul>
            </section>

            {/* Contact Information */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">14. Contact Information</h2>
              <p className="text-[#6B7280] mb-4">
                If you have any questions about these Terms, please contact us:
              </p>
              <div className="bg-[#ebf2fa] rounded-xl p-6">
                <p className="text-[#111827] font-medium mb-2">Vently Technologies Pvt. Ltd.</p>
                <p className="text-[#6B7280] text-sm mb-1">Email: legal@ventfly.com</p>
                <p className="text-[#6B7280] text-sm mb-1">Phone: +91 83688 01490</p>
                <p className="text-[#6B7280] text-sm">
                  Address: 123 Tech Park, Sector 18, Gurugram, Haryana 122015, India
                </p>
              </div>
            </section>

            {/* Acknowledgment */}
            <section className="mb-8">
              <div className="bg-[#807aeb]/5 border border-[#807aeb]/20 rounded-xl p-6">
                <h3 className="text-lg font-semibold text-[#111827] mb-3">Acknowledgment</h3>
                <p className="text-[#6B7280] text-sm">
                  By using Vently, you acknowledge that you have read, understood, and agree to be bound by these Terms and Conditions. These Terms constitute the entire agreement between you and Vently regarding your use of the Service.
                </p>
              </div>
            </section>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TermsAndConditions;