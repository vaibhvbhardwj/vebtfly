const PrivacyPolicy = () => {
  return (
    <div className="min-h-screen bg-[#ebf2fa] py-12">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold text-[#111827] mb-4">Privacy Policy</h1>
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
                At Vently Technologies Pvt. Ltd. ("we," "our," or "us"), we are committed to protecting your privacy and personal information. This Privacy Policy explains how we collect, use, disclose, and safeguard your information when you use our platform, website, and mobile application (collectively, the "Service").
              </p>
              <p className="text-[#6B7280] mb-4">
                By using our Service, you consent to the data practices described in this policy. If you do not agree with this policy, please do not use our Service.
              </p>
            </section>

            {/* Information We Collect */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">2. Information We Collect</h2>
              
              <h3 className="text-xl font-semibold text-[#111827] mb-3">2.1 Personal Information</h3>
              <p className="text-[#6B7280] mb-3">We collect personal information that you provide directly to us:</p>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li><strong>Account Information:</strong> Name, email address, phone number, password</li>
                <li><strong>Profile Information:</strong> Bio, skills, experience, organization details</li>
                <li><strong>Verification Data:</strong> Phone verification codes, identity documents (if required)</li>
                <li><strong>Payment Information:</strong> UPI IDs, payment transaction details (processed securely)</li>
                <li><strong>Communication Data:</strong> Messages, support tickets, feedback</li>
              </ul>

              <h3 className="text-xl font-semibold text-[#111827] mb-3">2.2 Event and Application Data</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Event details you create or apply for</li>
                <li>Application responses and volunteer preferences</li>
                <li>Attendance records and ratings</li>
                <li>Photos and media uploaded to events or profiles</li>
              </ul>

              <h3 className="text-xl font-semibold text-[#111827] mb-3">2.3 Automatically Collected Information</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li><strong>Device Information:</strong> IP address, browser type, operating system</li>
                <li><strong>Usage Data:</strong> Pages visited, features used, time spent on platform</li>
                <li><strong>Location Data:</strong> General location based on IP address (not precise GPS)</li>
                <li><strong>Cookies and Tracking:</strong> Session data, preferences, analytics</li>
              </ul>
            </section>

            {/* How We Use Information */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">3. How We Use Your Information</h2>
              
              <h3 className="text-xl font-semibold text-[#111827] mb-3">3.1 Primary Uses</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li><strong>Account Management:</strong> Create and maintain your account</li>
                <li><strong>Service Delivery:</strong> Facilitate event creation and volunteer matching</li>
                <li><strong>Communication:</strong> Send notifications, updates, and support responses</li>
                <li><strong>Payment Processing:</strong> Handle subscriptions and event payments</li>
                <li><strong>Verification:</strong> Verify phone numbers and prevent fraud</li>
              </ul>

              <h3 className="text-xl font-semibold text-[#111827] mb-3">3.2 Platform Improvement</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Analyze usage patterns to improve features</li>
                <li>Personalize your experience and recommendations</li>
                <li>Conduct research and analytics</li>
                <li>Develop new features and services</li>
              </ul>

              <h3 className="text-xl font-semibold text-[#111827] mb-3">3.3 Legal and Safety</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Comply with legal obligations and regulations</li>
                <li>Prevent fraud, abuse, and security threats</li>
                <li>Resolve disputes and enforce our Terms of Service</li>
                <li>Protect the rights and safety of users</li>
              </ul>
            </section>

            {/* Information Sharing */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">4. How We Share Your Information</h2>
              
              <h3 className="text-xl font-semibold text-[#111827] mb-3">4.1 With Other Users</h3>
              <div className="bg-[#807aeb]/5 border border-[#807aeb]/20 rounded-xl p-6 mb-4">
                <p className="text-[#6B7280] text-sm mb-3">
                  <strong>Profile Visibility:</strong> Your profile information (name, bio, skills, ratings) is visible to other users when you apply for events or create events.
                </p>
                <p className="text-[#6B7280] text-sm">
                  <strong>Contact Information:</strong> Phone numbers are only shared with confirmed event participants for coordination purposes.
                </p>
              </div>

              <h3 className="text-xl font-semibold text-[#111827] mb-3">4.2 With Service Providers</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li><strong>Payment Processors:</strong> Razorpay for secure payment processing</li>
                <li><strong>Cloud Storage:</strong> AWS for data storage and file hosting</li>
                <li><strong>Communication:</strong> SMS providers for phone verification</li>
                <li><strong>Analytics:</strong> Third-party analytics tools (anonymized data)</li>
                <li><strong>Support Tools:</strong> Customer service platforms</li>
              </ul>

              <h3 className="text-xl font-semibold text-[#111827] mb-3">4.3 Legal Requirements</h3>
              <p className="text-[#6B7280] mb-3">We may disclose your information when required by law or to:</p>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Comply with legal processes, court orders, or government requests</li>
                <li>Enforce our Terms of Service and other agreements</li>
                <li>Protect the rights, property, or safety of Vently, users, or the public</li>
                <li>Prevent or investigate fraud, security breaches, or illegal activities</li>
              </ul>

              <h3 className="text-xl font-semibold text-[#111827] mb-3">4.4 Business Transfers</h3>
              <p className="text-[#6B7280] mb-4">
                In the event of a merger, acquisition, or sale of assets, your information may be transferred to the new entity. We will notify you of any such change in ownership or control.
              </p>
            </section>

            {/* Data Security */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">5. Data Security</h2>
              
              <h3 className="text-xl font-semibold text-[#111827] mb-3">5.1 Security Measures</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li><strong>Encryption:</strong> Data encrypted in transit and at rest using industry standards</li>
                <li><strong>Access Controls:</strong> Strict access controls and authentication requirements</li>
                <li><strong>Regular Audits:</strong> Security assessments and vulnerability testing</li>
                <li><strong>Secure Infrastructure:</strong> AWS cloud infrastructure with enterprise-grade security</li>
                <li><strong>Payment Security:</strong> PCI DSS compliant payment processing</li>
              </ul>

              <h3 className="text-xl font-semibold text-[#111827] mb-3">5.2 Your Responsibilities</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Keep your password secure and don't share account credentials</li>
                <li>Log out of shared or public devices</li>
                <li>Report suspicious activity immediately</li>
                <li>Keep your contact information updated</li>
              </ul>

              <div className="bg-yellow-50 border border-yellow-200 rounded-xl p-6 mb-4">
                <h4 className="font-semibold text-[#111827] mb-2">
                  <i className="bx bx-info-circle text-yellow-500 mr-2" />
                  Important Security Note
                </h4>
                <p className="text-[#6B7280] text-sm">
                  While we implement robust security measures, no system is 100% secure. We cannot guarantee absolute security of your information, but we continuously work to protect your data using industry best practices.
                </p>
              </div>
            </section>

            {/* Data Retention */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">6. Data Retention</h2>
              
              <h3 className="text-xl font-semibold text-[#111827] mb-3">6.1 Retention Periods</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li><strong>Active Accounts:</strong> Data retained while your account is active</li>
                <li><strong>Deleted Accounts:</strong> Most data deleted within 30 days of account deletion</li>
                <li><strong>Legal Requirements:</strong> Some data retained longer for legal compliance</li>
                <li><strong>Financial Records:</strong> Payment data retained for 7 years as required by law</li>
                <li><strong>Dispute Records:</strong> Retained until disputes are resolved</li>
              </ul>

              <h3 className="text-xl font-semibold text-[#111827] mb-3">6.2 Data Minimization</h3>
              <p className="text-[#6B7280] mb-4">
                We only retain data that is necessary for providing our services, complying with legal obligations, or protecting our legitimate interests. We regularly review and delete unnecessary data.
              </p>
            </section>

            {/* Your Rights */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">7. Your Privacy Rights</h2>
              
              <h3 className="text-xl font-semibold text-[#111827] mb-3">7.1 Access and Control</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li><strong>Access:</strong> View and download your personal data</li>
                <li><strong>Correction:</strong> Update or correct inaccurate information</li>
                <li><strong>Deletion:</strong> Request deletion of your account and data</li>
                <li><strong>Portability:</strong> Export your data in a machine-readable format</li>
                <li><strong>Restriction:</strong> Limit how we process your data</li>
              </ul>

              <h3 className="text-xl font-semibold text-[#111827] mb-3">7.2 Communication Preferences</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Opt out of marketing emails (service emails will continue)</li>
                <li>Control push notifications in your account settings</li>
                <li>Manage SMS notifications for events and updates</li>
                <li>Set privacy preferences for profile visibility</li>
              </ul>

              <h3 className="text-xl font-semibold text-[#111827] mb-3">7.3 How to Exercise Your Rights</h3>
              <div className="bg-[#ebf2fa] rounded-xl p-6 mb-4">
                <p className="text-[#6B7280] text-sm mb-3">
                  To exercise any of these rights, you can:
                </p>
                <ul className="list-disc pl-6 space-y-1 text-[#6B7280] text-sm">
                  <li>Use the privacy controls in your account settings</li>
                  <li>Contact our support team at privacy@ventfly.com</li>
                  <li>Submit a request through our help center</li>
                  <li>Call us at +91 83688 01490</li>
                </ul>
              </div>
            </section>

            {/* Cookies and Tracking */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">8. Cookies and Tracking Technologies</h2>
              
              <h3 className="text-xl font-semibold text-[#111827] mb-3">8.1 Types of Cookies We Use</h3>
              <div className="space-y-4 mb-6">
                <div className="bg-[#10B981]/5 border border-[#10B981]/20 rounded-xl p-4">
                  <h4 className="font-semibold text-[#111827] mb-2">Essential Cookies</h4>
                  <p className="text-[#6B7280] text-sm">Required for basic platform functionality, login, and security.</p>
                </div>
                
                <div className="bg-blue-50 border border-blue-200 rounded-xl p-4">
                  <h4 className="font-semibold text-[#111827] mb-2">Performance Cookies</h4>
                  <p className="text-[#6B7280] text-sm">Help us understand how users interact with our platform to improve performance.</p>
                </div>
                
                <div className="bg-[#807aeb]/5 border border-[#807aeb]/20 rounded-xl p-4">
                  <h4 className="font-semibold text-[#111827] mb-2">Functional Cookies</h4>
                  <p className="text-[#6B7280] text-sm">Remember your preferences and settings for a better experience.</p>
                </div>
              </div>

              <h3 className="text-xl font-semibold text-[#111827] mb-3">8.2 Managing Cookies</h3>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>You can control cookies through your browser settings</li>
                <li>Disabling essential cookies may affect platform functionality</li>
                <li>We respect "Do Not Track" browser settings where possible</li>
                <li>Third-party cookies are governed by their respective privacy policies</li>
              </ul>
            </section>

            {/* Children's Privacy */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">9. Children's Privacy</h2>
              <div className="bg-red-50 border border-red-200 rounded-xl p-6 mb-4">
                <h3 className="font-semibold text-[#111827] mb-2">
                  <i className="bx bx-error text-[#EF4444] mr-2" />
                  Age Restriction
                </h3>
                <p className="text-[#6B7280] text-sm mb-3">
                  Our Service is not intended for children under 18 years of age. We do not knowingly collect personal information from children under 18.
                </p>
                <p className="text-[#6B7280] text-sm">
                  If you are a parent or guardian and believe your child has provided us with personal information, please contact us immediately so we can delete such information.
                </p>
              </div>
            </section>

            {/* International Users */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">10. International Data Transfers</h2>
              <p className="text-[#6B7280] mb-4">
                Your information may be transferred to and processed in countries other than your own, including the United States where our cloud infrastructure is located. We ensure appropriate safeguards are in place for such transfers.
              </p>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>Data processing agreements with service providers</li>
                <li>Compliance with applicable data protection laws</li>
                <li>Adequate security measures for international transfers</li>
                <li>Regular review of data transfer practices</li>
              </ul>
            </section>

            {/* Policy Updates */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">11. Changes to This Privacy Policy</h2>
              <ul className="list-disc pl-6 space-y-2 text-[#6B7280] mb-4">
                <li>We may update this Privacy Policy from time to time</li>
                <li>Material changes will be notified via email or platform notification</li>
                <li>Changes take effect 30 days after notification</li>
                <li>Continued use after changes constitutes acceptance</li>
                <li>Previous versions are archived and available upon request</li>
              </ul>
            </section>

            {/* Contact Information */}
            <section className="mb-8">
              <h2 className="text-2xl font-bold text-[#111827] mb-4">12. Contact Us</h2>
              <p className="text-[#6B7280] mb-4">
                If you have any questions, concerns, or requests regarding this Privacy Policy or our data practices, please contact us:
              </p>
              <div className="bg-[#ebf2fa] rounded-xl p-6">
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div>
                    <h4 className="font-semibold text-[#111827] mb-3">Privacy Officer</h4>
                    <p className="text-[#6B7280] text-sm mb-1">Email: privacy@ventfly.com</p>
                    <p className="text-[#6B7280] text-sm mb-1">Phone: +91 83688 01490</p>
                    <p className="text-[#6B7280] text-sm">Response time: Within 72 hours</p>
                  </div>
                  <div>
                    <h4 className="font-semibold text-[#111827] mb-3">Mailing Address</h4>
                    <p className="text-[#6B7280] text-sm">
                      Vently Technologies Pvt. Ltd.<br />
                      Attn: Privacy Officer<br />
                      123 Tech Park, Sector 18<br />
                      Gurugram, Haryana 122015<br />
                      India
                    </p>
                  </div>
                </div>
              </div>
            </section>

            {/* Commitment */}
            <section className="mb-8">
              <div className="bg-[#807aeb]/5 border border-[#807aeb]/20 rounded-xl p-6">
                <h3 className="text-lg font-semibold text-[#111827] mb-3">
                  <i className="bx bx-shield-check text-[#807aeb] mr-2" />
                  Our Privacy Commitment
                </h3>
                <p className="text-[#6B7280] text-sm mb-3">
                  We are committed to protecting your privacy and being transparent about our data practices. We believe privacy is a fundamental right and work continuously to earn and maintain your trust.
                </p>
                <p className="text-[#6B7280] text-sm">
                  We regularly review and update our privacy practices to ensure they meet the highest standards and comply with applicable laws and regulations.
                </p>
              </div>
            </section>
          </div>
        </div>
      </div>
    </div>
  );
};

export default PrivacyPolicy;