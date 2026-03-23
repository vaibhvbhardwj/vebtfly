import { useState } from 'react';

const ContactUs = () => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    subject: '',
    message: '',
    type: 'general'
  });
  const [loading, setLoading] = useState(false);
  const [success, setSuccess] = useState(false);

  const handleChange = (e) => {
    setFormData(prev => ({
      ...prev,
      [e.target.name]: e.target.value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    
    // Simulate form submission
    setTimeout(() => {
      setLoading(false);
      setSuccess(true);
      setFormData({ name: '', email: '', subject: '', message: '', type: 'general' });
    }, 1000);
  };

  return (
    <div className="min-h-screen bg-[#ebf2fa] py-12">
      <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
        
        {/* Header */}
        <div className="text-center mb-12">
          <h1 className="text-4xl font-bold text-[#111827] mb-4">Contact Us</h1>
          <p className="text-lg text-[#6B7280] max-w-2xl mx-auto">
            Have questions, feedback, or need support? We're here to help. Reach out to us through any of the channels below.
          </p>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">
          
          {/* Contact Information */}
          <div className="space-y-8">
            <div>
              <h2 className="text-2xl font-bold text-[#111827] mb-6">Get in Touch</h2>
              
              <div className="space-y-6">
                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 bg-[#807aeb]/10 rounded-xl flex items-center justify-center flex-shrink-0">
                    <i className="bx bx-envelope text-[#807aeb] text-xl" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-[#111827] mb-1">Email Support</h3>
                    <p className="text-[#6B7280] text-sm mb-2">For general inquiries and support</p>
                    <a href="mailto:support@ventfly.com" className="text-[#807aeb] hover:underline">
                      support@ventfly.com
                    </a>
                  </div>
                </div>

                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 bg-[#807aeb]/10 rounded-xl flex items-center justify-center flex-shrink-0">
                    <i className="bx bx-phone text-[#807aeb] text-xl" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-[#111827] mb-1">Phone Support</h3>
                    <p className="text-[#6B7280] text-sm mb-2">Monday to Friday, 9 AM - 6 PM IST</p>
                    <a href="tel:+918368801490" className="text-[#807aeb] hover:underline">
                      +91 83688 01490
                    </a>
                  </div>
                </div>

                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 bg-[#807aeb]/10 rounded-xl flex items-center justify-center flex-shrink-0">
                    <i className="bx bx-map text-[#807aeb] text-xl" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-[#111827] mb-1">Office Address</h3>
                    <p className="text-[#6B7280] text-sm">
                      Vently Technologies Pvt. Ltd.<br />
                      123 Tech Park, Sector 18<br />
                      Gurugram, Haryana 122015<br />
                      India
                    </p>
                  </div>
                </div>

                <div className="flex items-start gap-4">
                  <div className="w-12 h-12 bg-[#807aeb]/10 rounded-xl flex items-center justify-center flex-shrink-0">
                    <i className="bx bx-time text-[#807aeb] text-xl" />
                  </div>
                  <div>
                    <h3 className="font-semibold text-[#111827] mb-1">Response Time</h3>
                    <p className="text-[#6B7280] text-sm">
                      • Email: Within 24 hours<br />
                      • Phone: Immediate during business hours<br />
                      • Critical issues: Within 4 hours
                    </p>
                  </div>
                </div>
              </div>
            </div>

            {/* FAQ Quick Links */}
            <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-6">
              <h3 className="font-semibold text-[#111827] mb-4">Quick Help</h3>
              <div className="space-y-3">
                <a href="#" className="block text-[#807aeb] hover:underline text-sm">
                  <i className="bx bx-help-circle mr-2" />
                  How to verify my phone number?
                </a>
                <a href="#" className="block text-[#807aeb] hover:underline text-sm">
                  <i className="bx bx-help-circle mr-2" />
                  How to upgrade to Premium?
                </a>
                <a href="#" className="block text-[#807aeb] hover:underline text-sm">
                  <i className="bx bx-help-circle mr-2" />
                  Payment and refund policies
                </a>
                <a href="#" className="block text-[#807aeb] hover:underline text-sm">
                  <i className="bx bx-help-circle mr-2" />
                  How to report a dispute?
                </a>
              </div>
            </div>
          </div>

          {/* Contact Form */}
          <div className="bg-white rounded-2xl border border-[#807aeb]/10 p-8">
            <h2 className="text-2xl font-bold text-[#111827] mb-6">Send us a Message</h2>
            
            {success && (
              <div className="mb-6 p-4 bg-green-50 border border-green-200 rounded-xl">
                <div className="flex items-center gap-3">
                  <i className="bx bx-check-circle text-green-500 text-xl" />
                  <p className="text-green-700 text-sm">
                    Thank you! Your message has been sent successfully. We'll get back to you within 24 hours.
                  </p>
                </div>
              </div>
            )}

            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-[#111827] mb-2">
                    Full Name <span className="text-[#EF4444]">*</span>
                  </label>
                  <input
                    type="text"
                    name="name"
                    value={formData.name}
                    onChange={handleChange}
                    required
                    className="w-full px-4 py-3 bg-[#ebf2fa] border border-[#807aeb]/20 rounded-xl text-[#111827] placeholder-[#6B7280] focus:outline-none focus:border-[#807aeb] transition"
                    placeholder="Your full name"
                  />
                </div>
                <div>
                  <label className="block text-sm font-medium text-[#111827] mb-2">
                    Email Address <span className="text-[#EF4444]">*</span>
                  </label>
                  <input
                    type="email"
                    name="email"
                    value={formData.email}
                    onChange={handleChange}
                    required
                    className="w-full px-4 py-3 bg-[#ebf2fa] border border-[#807aeb]/20 rounded-xl text-[#111827] placeholder-[#6B7280] focus:outline-none focus:border-[#807aeb] transition"
                    placeholder="your@email.com"
                  />
                </div>
              </div>

              <div>
                <label className="block text-sm font-medium text-[#111827] mb-2">
                  Inquiry Type
                </label>
                <select
                  name="type"
                  value={formData.type}
                  onChange={handleChange}
                  className="w-full px-4 py-3 bg-[#ebf2fa] border border-[#807aeb]/20 rounded-xl text-[#111827] focus:outline-none focus:border-[#807aeb] transition"
                >
                  <option value="general">General Inquiry</option>
                  <option value="support">Technical Support</option>
                  <option value="billing">Billing & Payments</option>
                  <option value="partnership">Partnership</option>
                  <option value="feedback">Feedback</option>
                  <option value="report">Report an Issue</option>
                </select>
              </div>

              <div>
                <label className="block text-sm font-medium text-[#111827] mb-2">
                  Subject <span className="text-[#EF4444]">*</span>
                </label>
                <input
                  type="text"
                  name="subject"
                  value={formData.subject}
                  onChange={handleChange}
                  required
                  className="w-full px-4 py-3 bg-[#ebf2fa] border border-[#807aeb]/20 rounded-xl text-[#111827] placeholder-[#6B7280] focus:outline-none focus:border-[#807aeb] transition"
                  placeholder="Brief description of your inquiry"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-[#111827] mb-2">
                  Message <span className="text-[#EF4444]">*</span>
                </label>
                <textarea
                  name="message"
                  value={formData.message}
                  onChange={handleChange}
                  required
                  rows="5"
                  className="w-full px-4 py-3 bg-[#ebf2fa] border border-[#807aeb]/20 rounded-xl text-[#111827] placeholder-[#6B7280] focus:outline-none focus:border-[#807aeb] transition resize-none"
                  placeholder="Please provide as much detail as possible..."
                />
              </div>

              <button
                type="submit"
                disabled={loading}
                className="w-full py-3 bg-[#807aeb] text-white font-semibold rounded-xl hover:bg-[#6b64d4] transition disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {loading ? (
                  <span className="flex items-center justify-center gap-2">
                    <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                    Sending...
                  </span>
                ) : (
                  'Send Message'
                )}
              </button>
            </form>
          </div>
        </div>

        {/* Additional Information */}
        <div className="mt-16 grid grid-cols-1 md:grid-cols-3 gap-8">
          <div className="text-center">
            <div className="w-16 h-16 bg-[#807aeb]/10 rounded-2xl flex items-center justify-center mx-auto mb-4">
              <i className="bx bx-support text-[#807aeb] text-2xl" />
            </div>
            <h3 className="font-semibold text-[#111827] mb-2">24/7 Support</h3>
            <p className="text-[#6B7280] text-sm">
              Our support team is available around the clock to help you with any issues.
            </p>
          </div>
          
          <div className="text-center">
            <div className="w-16 h-16 bg-[#807aeb]/10 rounded-2xl flex items-center justify-center mx-auto mb-4">
              <i className="bx bx-shield-check text-[#807aeb] text-2xl" />
            </div>
            <h3 className="font-semibold text-[#111827] mb-2">Secure Platform</h3>
            <p className="text-[#6B7280] text-sm">
              Your data and privacy are protected with enterprise-grade security measures.
            </p>
          </div>
          
          <div className="text-center">
            <div className="w-16 h-16 bg-[#807aeb]/10 rounded-2xl flex items-center justify-center mx-auto mb-4">
              <i className="bx bx-rocket text-[#807aeb] text-2xl" />
            </div>
            <h3 className="font-semibold text-[#111827] mb-2">Fast Response</h3>
            <p className="text-[#6B7280] text-sm">
              We pride ourselves on quick response times and efficient problem resolution.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ContactUs;