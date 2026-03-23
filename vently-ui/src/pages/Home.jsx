import { Link } from 'react-router-dom';
import { Events } from '../assets';
import { useAuthStore } from '../store/authStore';

const Home = () => {
  const { isAuthenticated, user } = useAuthStore();

  const dashboardLink = user?.role === 'VOLUNTEER'
    ? '/volunteer/dashboard'
    : user?.role === 'ORGANIZER'
    ? '/organizer/dashboard'
    : user?.role === 'ADMIN'
    ? '/admin/dashboard'
    : null;

  return (
    <div className="bg-[#ebf2fa] text-[#111827] font-sans overflow-x-hidden">

      {/* HERO */}
      <section className="relative min-h-[90vh] flex items-center py-20 px-6 overflow-hidden">
        {/* Background blobs */}
        <div className="absolute top-0 right-0 w-[600px] h-[600px] bg-[#807aeb] rounded-full opacity-10 blur-3xl -mr-72 -mt-72 pointer-events-none" />
        <div className="absolute bottom-0 left-0 w-[400px] h-[400px] bg-[#10B981] rounded-full opacity-5 blur-3xl -ml-48 -mb-48 pointer-events-none" />
        <div className="absolute top-1/2 left-1/2 w-[300px] h-[300px] bg-[#807aeb] rounded-full opacity-5 blur-3xl -translate-x-1/2 -translate-y-1/2 pointer-events-none" />

        <div className="max-w-6xl mx-auto flex flex-col md:flex-row items-center relative z-10 gap-16 w-full">
          <div className="md:w-1/2 animate-fade-in">
            {/* Live badge */}
            <div className="inline-flex items-center gap-2 bg-white text-[#807aeb] px-4 py-2 rounded-full text-sm font-semibold mb-8 border border-[#807aeb]/20 shadow-sm">
              <span className="w-2 h-2 bg-[#10B981] rounded-full animate-pulse" />
              Now live across India
            </div>

            <h1 className="text-5xl md:text-6xl font-extrabold mb-6 leading-[1.1] text-[#111827]">
              Where <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#807aeb] to-[#6b64d4]">Talent</span><br />
              Meets Opportunity
            </h1>

            <p className="text-lg text-[#6B7280] mb-10 leading-relaxed max-w-md">
              Vently connects skilled volunteers with professional event organizers. Apply to events, earn money, and build a reputation that opens doors.
            </p>

            <div className="flex gap-4 flex-wrap">
              {isAuthenticated && dashboardLink ? (
                <Link
                  to={dashboardLink}
                  className="bg-gradient-to-r from-[#807aeb] to-[#6b64d4] text-white px-8 py-4 rounded-xl font-bold shadow-lg shadow-[#807aeb]/30 hover:shadow-xl hover:shadow-[#807aeb]/40 hover:-translate-y-0.5 transition-all duration-200"
                >
                  Go to Dashboard →
                </Link>
              ) : (
                <>
                  <Link
                    to="/register"
                    className="bg-gradient-to-r from-[#807aeb] to-[#6b64d4] text-white px-8 py-4 rounded-xl font-bold shadow-lg shadow-[#807aeb]/30 hover:shadow-xl hover:shadow-[#807aeb]/40 hover:-translate-y-0.5 transition-all duration-200"
                  >
                    Get Started Free
                  </Link>
                  <Link
                    to="/how-it-works"
                    className="bg-white border border-[#807aeb]/20 text-[#807aeb] px-8 py-4 rounded-xl font-bold hover:bg-[#807aeb]/5 hover:border-[#807aeb]/40 transition-all duration-200 shadow-sm"
                  >
                    How It Works
                  </Link>
                </>
              )}
            </div>

            {/* Trust indicators */}
            <div className="flex items-center gap-6 mt-10 flex-wrap">
              <div className="flex -space-x-2">
                {['👩‍💼', '👨‍💼', '👩‍🎓', '👨‍🎤'].map((emoji, i) => (
                  <div key={i} className="w-9 h-9 rounded-full bg-white border-2 border-[#ebf2fa] flex items-center justify-center text-base shadow-sm">
                    {emoji}
                  </div>
                ))}
              </div>
              <p className="text-sm text-[#6B7280]">
                <span className="font-semibold text-[#111827]">5,000+</span> volunteers already earning
              </p>
            </div>
          </div>

          <div className="md:w-1/2 flex justify-center animate-slide-up">
            <div className="relative w-full max-w-lg">
              {/* Decorative card behind image */}
              <div className="absolute -top-4 -right-4 w-full h-full bg-gradient-to-br from-[#807aeb]/20 to-[#807aeb]/5 rounded-3xl" />
              <div className="absolute inset-0 bg-[#807aeb] rounded-3xl opacity-10 blur-2xl scale-105" />
              <img
                src={Events}
                alt="Volunteers at an event"
                className="relative w-full rounded-3xl shadow-2xl border border-white/50"
              />
              {/* Floating stat card */}
              <div className="absolute -bottom-5 -left-5 bg-white rounded-2xl shadow-xl px-5 py-3 flex items-center gap-3 border border-[#807aeb]/10">
                <div className="w-10 h-10 bg-[#10B981]/10 rounded-xl flex items-center justify-center text-xl">💸</div>
                <div>
                  <p className="text-xs text-[#6B7280]">Avg. earnings</p>
                  <p className="font-bold text-[#111827]">₹12,000/month</p>
                </div>
              </div>
              {/* Floating badge */}
              <div className="absolute -top-4 -left-4 bg-white rounded-2xl shadow-xl px-4 py-2 flex items-center gap-2 border border-[#807aeb]/10">
                <span className="text-yellow-400 text-sm">★★★★★</span>
                <p className="text-xs font-semibold text-[#111827]">4.9 rated</p>
              </div>
            </div>
          </div>
        </div>
      </section>

      {/* STATS BAR */}
      <div className="bg-white border-y border-[#807aeb]/10 py-8 px-6">
        <div className="max-w-5xl mx-auto grid grid-cols-2 md:grid-cols-4 gap-6">
          {[
            { label: 'Verified Events', count: '1,000+', icon: '✨', color: 'text-[#807aeb]' },
            { label: 'Active Volunteers', count: '5,000+', icon: '👥', color: 'text-[#10B981]' },
            { label: 'Cities Covered', count: '25+', icon: '📍', color: 'text-orange-500' },
            { label: 'Secure Payments', count: '100%', icon: '🔒', color: 'text-blue-500' },
          ].map((stat, i) => (
            <div key={i} className="flex items-center gap-4">
              <div className="w-12 h-12 bg-[#ebf2fa] rounded-xl flex items-center justify-center text-2xl flex-shrink-0">
                {stat.icon}
              </div>
              <div>
                <p className={`text-2xl font-extrabold ${stat.color}`}>{stat.count}</p>
                <p className="text-[#6B7280] text-sm">{stat.label}</p>
              </div>
            </div>
          ))}
        </div>
      </div>

      {/* HOW IT WORKS */}
      <section className="py-24 max-w-6xl mx-auto px-6">
        <div className="text-center mb-16">
          <span className="text-xs font-bold uppercase tracking-widest text-[#807aeb] bg-[#807aeb]/10 px-4 py-2 rounded-full">Simple Process</span>
          <h2 className="text-4xl font-extrabold mt-4 mb-3 text-[#111827]">How Vently Works</h2>
          <p className="text-[#6B7280] text-lg max-w-xl mx-auto">From sign-up to payout in three easy steps</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-20 relative">
          {/* Connector line */}
          <div className="hidden md:block absolute top-10 left-1/4 right-1/4 h-0.5 bg-gradient-to-r from-[#807aeb]/20 via-[#807aeb]/40 to-[#807aeb]/20" />

          {[
            { step: '01', title: 'Create Your Profile', desc: 'Sign up as a volunteer or organizer in minutes. Add your skills, experience, and availability to stand out.', icon: '👤' },
            { step: '02', title: 'Connect & Apply', desc: 'Browse events near you. Apply with one click. Organizers review your profile and select the best fit.', icon: '🤝' },
            { step: '03', title: 'Earn & Build Reputation', desc: 'Attend events, get paid securely through escrow, and collect ratings that boost your profile.', icon: '🏆' },
          ].map((item, i) => (
            <div key={i} className="bg-white p-8 rounded-3xl border border-[#807aeb]/10 shadow-sm hover:shadow-md hover:-translate-y-1 transition-all duration-300 relative">
              <div className="text-4xl mb-5">{item.icon}</div>
              <span className="text-xs font-bold text-[#807aeb]/40 tracking-widest">{item.step}</span>
              <h3 className="text-xl font-bold mt-1 mb-3 text-[#111827]">{item.title}</h3>
              <p className="text-[#6B7280] leading-relaxed text-sm">{item.desc}</p>
            </div>
          ))}
        </div>

        {/* Benefits split */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
          {[
            {
              role: 'For Volunteers',
              icon: '🙋',
              color: 'from-[#10B981]/10 to-[#10B981]/5',
              accent: 'text-[#10B981]',
              border: 'border-[#10B981]/20',
              items: [
                'Apply to events that match your schedule',
                'Earn money and build your professional CV',
                'Payments secured through escrow — always safe',
                'Get verified and grow your reputation',
                'Access exclusive premium events',
              ],
            },
            {
              role: 'For Organizers',
              icon: '🏢',
              color: 'from-[#807aeb]/10 to-[#807aeb]/5',
              accent: 'text-[#807aeb]',
              border: 'border-[#807aeb]/20',
              items: [
                'Post events and reach thousands of volunteers',
                'Review profiles with ratings and verification badges',
                'Manage attendance with QR codes or Excel upload',
                'Automated payments — no manual transfers',
                'Build a trusted team for future events',
              ],
            },
          ].map((card, i) => (
            <div key={i} className={`bg-gradient-to-br ${card.color} p-8 rounded-3xl border ${card.border}`}>
              <div className="flex items-center gap-3 mb-6">
                <div className="w-12 h-12 bg-white rounded-2xl flex items-center justify-center text-2xl shadow-sm">{card.icon}</div>
                <h3 className={`text-xl font-bold ${card.accent}`}>{card.role}</h3>
              </div>
              <ul className="space-y-3">
                {card.items.map((item, j) => (
                  <li key={j} className="flex items-start gap-3 text-[#374151] text-sm">
                    <span className={`w-5 h-5 ${card.accent} bg-white rounded-full flex items-center justify-center text-xs font-bold flex-shrink-0 mt-0.5 shadow-sm`}>✓</span>
                    {item}
                  </li>
                ))}
              </ul>
              <Link
                to="/register"
                className={`mt-8 inline-flex items-center gap-2 ${card.accent} font-semibold text-sm hover:gap-3 transition-all`}
              >
                Get started <span>→</span>
              </Link>
            </div>
          ))}
        </div>
      </section>

      {/* FEATURED EVENTS */}
      <section className="bg-white py-24 px-6">
        <div className="max-w-6xl mx-auto">
          <div className="flex justify-between items-end mb-14 flex-wrap gap-4">
            <div>
              <span className="text-xs font-bold uppercase tracking-widest text-[#807aeb] bg-[#807aeb]/10 px-4 py-2 rounded-full">Live Now</span>
              <h2 className="text-4xl font-extrabold text-[#111827] mt-4">Featured Events</h2>
              <p className="text-[#6B7280] mt-2">Be part of the next big thing</p>
            </div>
            <Link to="/register" className="text-[#807aeb] font-semibold hover:underline text-sm flex items-center gap-1">
              View All Events <span>→</span>
            </Link>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {[
              { title: 'Sunburn Music Fest', location: 'Goa', date: 'Dec 28', reward: '₹2,000/day', category: 'Concert', volunteers: 15, emoji: '🎵' },
              { title: 'Tech-X Expo 2026', location: 'Mumbai', date: 'Jan 15', reward: '₹1,500/day', category: 'Exhibition', volunteers: 8, emoji: '💻' },
              { title: 'Grand Wedding Decor', location: 'Delhi', date: 'Feb 20', reward: '₹1,800/day', category: 'Wedding', volunteers: 12, emoji: '💍' },
            ].map((item, idx) => (
              <div key={idx} className="bg-[#ebf2fa] rounded-3xl overflow-hidden border border-[#807aeb]/10 hover:shadow-lg hover:-translate-y-1 transition-all duration-300 group">
                <div className="h-40 bg-gradient-to-br from-[#807aeb]/20 to-[#807aeb]/40 flex items-center justify-center relative overflow-hidden">
                  <span className="text-6xl opacity-40 group-hover:scale-110 transition-transform duration-300">{item.emoji}</span>
                  <div className="absolute top-3 right-3 bg-white/80 backdrop-blur-sm text-[#807aeb] text-xs font-bold px-3 py-1 rounded-full border border-[#807aeb]/20">
                    {item.category}
                  </div>
                </div>
                <div className="p-5">
                  <div className="flex items-center justify-between mb-2">
                    <h3 className="text-base font-bold text-[#111827]">{item.title}</h3>
                    <span className="text-xs text-[#6B7280] bg-white px-2 py-1 rounded-full">{item.volunteers} spots</span>
                  </div>
                  <p className="text-[#6B7280] text-sm">📍 {item.location} &nbsp;·&nbsp; 📅 {item.date}</p>
                  <div className="mt-4 flex justify-between items-center">
                    <span className="font-extrabold text-[#807aeb] text-lg">{item.reward}</span>
                    <Link
                      to="/register"
                      className="bg-[#807aeb] text-white px-4 py-2 rounded-xl text-sm font-semibold hover:bg-[#6b64d4] transition shadow-sm"
                    >
                      Apply Now
                    </Link>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* TESTIMONIALS */}
      <section className="py-24 px-6 max-w-6xl mx-auto">
        <div className="text-center mb-16">
          <span className="text-xs font-bold uppercase tracking-widest text-[#807aeb] bg-[#807aeb]/10 px-4 py-2 rounded-full">Real Stories</span>
          <h2 className="text-4xl font-extrabold mt-4 mb-3 text-[#111827]">What Our Users Say</h2>
          <p className="text-[#6B7280]">Thousands of volunteers and organizers trust Vently</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {[
            { name: 'Priya Sharma', role: 'Volunteer · Mumbai', text: 'Vently helped me earn ₹15,000 in just 3 months! The platform is secure and the organizers are professional.', avatar: '👩‍💼', rating: 5 },
            { name: 'Rajesh Kumar', role: 'Event Organizer · Delhi', text: 'Finding reliable volunteers has never been easier. The verification system gives me confidence in my team.', avatar: '👨‍💼', rating: 5 },
            { name: 'Ananya Patel', role: 'Volunteer · Bangalore', text: 'Great platform with transparent payments and fair treatment. I\'ve made amazing connections here!', avatar: '👩‍🎓', rating: 5 },
          ].map((t, idx) => (
            <div key={idx} className="bg-white p-7 rounded-3xl border border-[#807aeb]/10 shadow-sm hover:shadow-md hover:-translate-y-1 transition-all duration-300">
              <div className="flex gap-0.5 mb-4">
                {[...Array(t.rating)].map((_, i) => <span key={i} className="text-yellow-400">★</span>)}
              </div>
              <p className="text-[#374151] text-sm leading-relaxed mb-6 italic">"{t.text}"</p>
              <div className="flex items-center gap-3 pt-4 border-t border-[#ebf2fa]">
                <span className="text-3xl">{t.avatar}</span>
                <div>
                  <p className="font-bold text-[#111827] text-sm">{t.name}</p>
                  <p className="text-xs text-[#6B7280]">{t.role}</p>
                </div>
              </div>
            </div>
          ))}
        </div>
      </section>

      {/* FAQ */}
      <section className="bg-white py-24 px-6">
        <div className="max-w-3xl mx-auto">
          <div className="text-center mb-14">
            <span className="text-xs font-bold uppercase tracking-widest text-[#807aeb] bg-[#807aeb]/10 px-4 py-2 rounded-full">FAQ</span>
            <h2 className="text-4xl font-extrabold mt-4 mb-3 text-[#111827]">Common Questions</h2>
          </div>
          <div className="space-y-4">
            {[
              { q: 'Is Vently free to use?', a: 'Yes! The basic plan is completely free. You can apply to up to 5 events per month as a volunteer, or post up to 3 events as an organizer. Upgrade to Premium for unlimited access.' },
              { q: 'How does payment work?', a: 'Organizers deposit funds into escrow before the event. Once you mark attendance, funds are automatically released to your account. No manual transfers, no delays.' },
              { q: 'What if an organizer cancels the event?', a: 'If an organizer cancels more than 7 days before the event, you receive a full refund. Cancellations within 7 days receive a 50% refund. Within 24 hours, no refund is issued.' },
              { q: 'How do I get verified?', a: 'Complete your profile, add your skills and experience, and request verification from the admin. Verified volunteers get a badge that boosts their visibility to organizers.' },
            ].map((faq, i) => (
              <details key={i} className="group bg-[#ebf2fa] rounded-2xl border border-[#807aeb]/10 overflow-hidden">
                <summary className="flex items-center justify-between px-6 py-4 cursor-pointer font-semibold text-[#111827] text-sm list-none">
                  {faq.q}
                  <span className="text-[#807aeb] group-open:rotate-45 transition-transform duration-200 text-xl leading-none">+</span>
                </summary>
                <p className="px-6 pb-5 text-[#6B7280] text-sm leading-relaxed">{faq.a}</p>
              </details>
            ))}
          </div>
        </div>
      </section>

      {/* CTA */}
      <section className="relative py-24 px-6 overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-br from-[#807aeb] to-[#6b64d4]" />
        <div className="absolute top-0 right-0 w-96 h-96 bg-white rounded-full opacity-5 blur-3xl -mr-48 -mt-48" />
        <div className="absolute bottom-0 left-0 w-96 h-96 bg-white rounded-full opacity-5 blur-3xl -ml-48 -mb-48" />

        <div className="max-w-4xl mx-auto text-center relative z-10">
          <h2 className="text-4xl md:text-5xl font-extrabold text-white mb-4">Ready to Get Started?</h2>
          <p className="text-white/80 text-lg mb-10 max-w-xl mx-auto">
            Join thousands of volunteers and organizers building their future on Vently.
          </p>
          <div className="flex gap-4 justify-center flex-wrap">
            <Link
              to="/register"
              className="bg-white text-[#807aeb] px-8 py-4 rounded-xl font-bold hover:shadow-xl hover:-translate-y-0.5 transition-all duration-200"
            >
              Sign Up as Volunteer
            </Link>
            <Link
              to="/register"
              className="border-2 border-white/40 text-white px-8 py-4 rounded-xl font-bold hover:bg-white/10 hover:border-white/60 transition-all duration-200"
            >
              Sign Up as Organizer
            </Link>
          </div>
          <p className="text-white/50 text-sm mt-6">No credit card required · Free forever plan available</p>
        </div>
      </section>


    </div>
  );
};

export default Home;
