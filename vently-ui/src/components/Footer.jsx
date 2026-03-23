import { Link } from 'react-router-dom';

const Footer = () => {
  return (
    <footer className="bg-white border-t border-[#807aeb]/10 mt-16">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8">
          
          {/* Company Info */}
          <div className="col-span-1 md:col-span-2">
            <div className="flex items-center gap-2 mb-4">
              <div className="w-8 h-8 bg-[#807aeb] rounded-lg flex items-center justify-center">
                <span className="text-white font-bold text-sm">V</span>
              </div>
              <span className="text-xl font-bold text-[#111827]">Vently</span>
            </div>
            <p className="text-[#6B7280] text-sm mb-4 max-w-md">
              Connecting volunteers with meaningful opportunities. Join thousands of volunteers and organizers making a difference in their communities.
            </p>
            <div className="flex items-center gap-4">
              <a href="mailto:support@ventfly.com" className="text-[#6B7280] hover:text-[#807aeb] transition">
                <i className="bx bx-envelope text-xl" />
              </a>
              <a href="tel:+918368801490" className="text-[#6B7280] hover:text-[#807aeb] transition">
                <i className="bx bx-phone text-xl" />
              </a>
              <a href="#" className="text-[#6B7280] hover:text-[#807aeb] transition">
                <i className="bx bxl-twitter text-xl" />
              </a>
              <a href="#" className="text-[#6B7280] hover:text-[#807aeb] transition">
                <i className="bx bxl-linkedin text-xl" />
              </a>
            </div>
          </div>

          {/* Quick Links */}
          <div>
            <h3 className="font-semibold text-[#111827] mb-4">Platform</h3>
            <ul className="space-y-3">
              <li>
                <Link to="/events" className="text-[#6B7280] hover:text-[#807aeb] text-sm transition">
                  Browse Events
                </Link>
              </li>
              <li>
                <Link to="/events/create" className="text-[#6B7280] hover:text-[#807aeb] text-sm transition">
                  Post Event
                </Link>
              </li>
              <li>
                <Link to="/how-it-works" className="text-[#6B7280] hover:text-[#807aeb] text-sm transition">
                  How It Works
                </Link>
              </li>
              <li>
                <Link to="/subscription" className="text-[#6B7280] hover:text-[#807aeb] text-sm transition">
                  Pricing
                </Link>
              </li>
            </ul>
          </div>

          {/* Legal & Support */}
          <div>
            <h3 className="font-semibold text-[#111827] mb-4">Support</h3>
            <ul className="space-y-3">
              <li>
                <Link to="/contact" className="text-[#6B7280] hover:text-[#807aeb] text-sm transition">
                  Contact Us
                </Link>
              </li>
              <li>
                <Link to="/terms" className="text-[#6B7280] hover:text-[#807aeb] text-sm transition">
                  Terms & Conditions
                </Link>
              </li>
              <li>
                <Link to="/privacy" className="text-[#6B7280] hover:text-[#807aeb] text-sm transition">
                  Privacy Policy
                </Link>
              </li>
              <li>
                <Link to="/refunds" className="text-[#6B7280] hover:text-[#807aeb] text-sm transition">
                  Refund Policy
                </Link>
              </li>
            </ul>
          </div>
        </div>

        {/* Bottom Bar */}
        <div className="border-t border-[#807aeb]/10 mt-8 pt-8 flex flex-col sm:flex-row justify-between items-center gap-4">
          <p className="text-[#6B7280] text-sm">
            © 2026 Vently Technologies Pvt. Ltd. All rights reserved.
          </p>
          <div className="flex items-center gap-6">
            <span className="text-[#6B7280] text-xs">Made with ❤️ in India</span>
            <div className="flex items-center gap-2">
              <i className="bx bx-shield-check text-[#10B981]" />
              <span className="text-[#6B7280] text-xs">Secure Platform</span>
            </div>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;