import React, { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { Events } from '../assets';
import { useAuth } from '../hooks/useAuth';

const Home = () => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();

  useEffect(() => {
    if (isAuthenticated && user) {
      if (user.role === 'VOLUNTEER') navigate('/volunteer/dashboard');
      else if (user.role === 'ORGANIZER') navigate('/organizer/dashboard');
      else if (user.role === 'ADMIN') navigate('/admin/dashboard');
    }
  }, [isAuthenticated, user, navigate]);

  if (isAuthenticated) return null;

  return (
    <div className="bg-[#ebf2fa] text-[#111827] font-sans">

      {/* HERO */}
      <section className="relative py-24 px-6 overflow-hidden">
        <div className="absolute top-0 right-0 w-[500px] h-[500px] bg-[#807aeb] rounded-full opacity-10 blur-3xl -mr-64 -mt-64" />
        <div className="absolute bottom-0 left-0 w-96 h-96 bg-[#807aeb] rounded-full opacity-5 blur-3xl -ml-48 -mb-48" />

        <div className="max-w-6xl mx-auto flex flex-col md:flex-row items-center relative z-10 gap-12">
          <div className="md:w-1/2 animate-fade-in">
            <div className="inline-flex items-center gap-2 bg-[#807aeb]/10 text-[#807aeb] px-4 py-2 rounded-full text-sm font-semibold mb-6 border border-[#807aeb]/20">
              <span className="w-2 h-2 bg-[#10B981] rounded-full animate-pulse" />
              Now live in India
            </div>
            <h1 className="text-5xl md:text-6xl font-bold mb-6 leading-tight text-[#111827]">
              Connecting <span className="text-[#807aeb]">Talent</span> to<br />
              Real Opportunities
            </h1>
            <p className="text-lg text-[#6B7280] mb-8 leading-relaxed">
              Find events. Build skills. Host incredible events. Ventfly is where professional organizers meet energetic volunteers.
            </p>
            <div className="flex gap-4 flex-wrap">
              <Link to="/register" className="bg-[#807aeb] text-white px-8 py-4 rounded-xl font-bold hover:bg-[#6b64d4] hover:shadow-lg hover:shadow-[#807aeb]/30 hover:-translate-y-0.5">
                Get Started Free
              </Link>
              <Link to="/how-it-works" className="border-2 border-[#807aeb]/30 text-[#807aeb] px-8 py-4 rounded-xl font-bold hover:bg-[#807aeb]/10">
                How It Works
              </Link>
            </div>
          </div>
          <div className="md:w-1/2 flex justify-center animate-slide-up">
            <div className="relative">
              <div className="absolute inset-0 bg-[#807aeb] rounded-3xl opacity-10 blur-2xl scale-110" />
              <img src={Events} alt="Collaboration" className="relative w-full max-w-md drop-shadow-xl rounded-2xl" />
            </div>
          </div>
        </div>
      </section>

      {/* STATS */}
      <div className="max-w-5xl mx-auto grid grid-cols-1 md:grid-cols-3 gap-6 px-6 mb-20">
        {[
          { label: "Verified Events", count: "1000+", icon: "✨" },
          { label: "Active Volunteers", count: "5000+", icon: "👥" },
          { label: "Secure Payments", count: "100%", icon: "🔒" },
        ].map((stat, i) => (
          <div
            key={i}
            className={`bg-white p-6 rounded-2xl shadow-sm border border-[#807aeb]/10 flex items-center gap-4 card-hover animate-slide-up animate-stagger-${i + 1}`}
          >
            <div className="w-12 h-12 bg-[#807aeb]/10 rounded-xl flex items-center justify-center text-2xl">
              {stat.icon}
            </div>
            <div>
              <h3 className="text-2xl font-bold text-[#111827]">{stat.count}</h3>
              <p className="text-[#6B7280] text-sm">{stat.label}</p>
            </div>
          </div>
        ))}
      </div>

      {/* HOW IT WORKS */}
      <section className="py-20 max-w-6xl mx-auto px-6">
        <div className="text-center mb-14">
          <h2 className="text-4xl font-bold mb-3 text-[#111827]">How It Works</h2>
          <div className="h-1 w-16 bg-[#807aeb] mx-auto rounded-full mb-4" />
          <p className="text-[#6B7280] text-lg">Three simple steps to get started</p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-8 mb-16">
          {[
            { step: "1", title: "Sign Up", desc: "Create your account as a volunteer or organizer in minutes. Verify your phone and complete your profile." },
            { step: "2", title: "Connect", desc: "Volunteers browse events and apply. Organizers review applications and select their team." },
            { step: "3", title: "Earn & Grow", desc: "Complete events, get paid securely, and build your reputation with ratings and reviews." },
          ].map((item, i) => (
            <div key={i} className={`bg-white p-8 rounded-2xl border border-[#807aeb]/10 shadow-sm card-hover animate-slide-up animate-stagger-${i + 1}`}>
              <div className="w-10 h-10 bg-[#807aeb] text-white rounded-xl flex items-center justify-center font-bold text-lg mb-5">
                {item.step}
              </div>
              <h3 className="text-xl font-bold mb-3 text-[#111827]">{item.title}</h3>
              <p className="text-[#6B7280] leading-relaxed">{item.desc}</p>
            </div>
          ))}
        </div>

        {/* Benefits */}
        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <div className="bg-white p-8 rounded-2xl border border-[#807aeb]/10 shadow-sm card-hover">
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 bg-[#807aeb]/10 rounded-xl flex items-center justify-center text-xl">🙋</div>
              <h3 className="text-xl font-bold text-[#111827]">For Volunteers</h3>
            </div>
            <ul className="space-y-3">
              {["Apply to events that fit your schedule", "Earn money and build your CV", "Secure payments through escrow", "Get verified and build your reputation"].map((item, i) => (
                <li key={i} className="flex items-center gap-3 text-[#6B7280]">
                  <span className="w-5 h-5 bg-[#10B981]/10 text-[#10B981] rounded-full flex items-center justify-center text-xs font-bold flex-shrink-0">✓</span>
                  {item}
                </li>
              ))}
            </ul>
          </div>
          <div className="bg-white p-8 rounded-2xl border border-[#807aeb]/10 shadow-sm card-hover">
            <div className="flex items-center gap-3 mb-6">
              <div className="w-10 h-10 bg-[#807aeb]/10 rounded-xl flex items-center justify-center text-xl">🏢</div>
              <h3 className="text-xl font-bold text-[#111827]">For Organizers</h3>
            </div>
            <ul className="space-y-3">
              {["Post events and reach verified volunteers", "Manage attendance and payments easily", "Access detailed volunteer profiles", "Build a trusted team for future events"].map((item, i) => (
                <li key={i} className="flex items-center gap-3 text-[#6B7280]">
                  <span className="w-5 h-5 bg-[#807aeb]/10 text-[#807aeb] rounded-full flex items-center justify-center text-xs font-bold flex-shrink-0">✓</span>
                  {item}
                </li>
              ))}
            </ul>
          </div>
        </div>
      </section>

      {/* FEATURED EVENTS */}
      <section className="bg-white py-20 px-6">
        <div className="max-w-6xl mx-auto">
          <div className="flex justify-between items-end mb-12">
            <div>
              <h2 className="text-4xl font-bold text-[#111827]">Featured Events</h2>
              <p className="text-[#6B7280] mt-2">Be part of the next big thing</p>
            </div>
            <Link to="/register" className="text-[#807aeb] font-semibold hover:underline text-sm">View All →</Link>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {[
              { title: "Sunburn Music Fest", location: "Goa", date: "Dec 28", reward: "₹2000/day", category: "Concert", volunteers: 15 },
              { title: "Tech-X Expo", location: "Mumbai", date: "Nov 15", reward: "₹1500/day", category: "Exhibition", volunteers: 8 },
              { title: "Grand Wedding Decor", location: "Delhi", date: "Oct 20", reward: "₹1800/day", category: "Wedding", volunteers: 12 },
            ].map((item, idx) => (
              <div key={idx} className="bg-[#ebf2fa] rounded-2xl overflow-hidden border border-[#807aeb]/10 card-hover">
                <div className="h-36 bg-gradient-to-br from-[#807aeb]/20 to-[#807aeb]/40 flex items-center justify-center">
                  <span className="text-4xl opacity-50">🎪</span>
                </div>
                <div className="p-5">
                  <div className="flex items-center justify-between mb-3">
                    <span className="bg-[#807aeb]/10 text-[#807aeb] text-xs font-semibold px-3 py-1 rounded-full border border-[#807aeb]/20">{item.category}</span>
                    <span className="text-xs text-[#6B7280]">{item.volunteers} spots</span>
                  </div>
                  <h3 className="text-lg font-bold text-[#111827]">{item.title}</h3>
                  <p className="text-[#6B7280] text-sm mt-1">📍 {item.location} · 📅 {item.date}</p>
                  <div className="mt-4 flex justify-between items-center">
                    <span className="font-bold text-[#807aeb]">{item.reward}</span>
                    <Link to="/register" className="bg-[#807aeb] text-white px-4 py-2 rounded-lg text-sm font-medium hover:bg-[#6b64d4]">Apply Now</Link>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </div>
      </section>

      {/* TESTIMONIALS */}
      <section className="py-20 px-6 max-w-6xl mx-auto">
        <div className="text-center mb-14">
          <h2 className="text-4xl font-bold mb-3 text-[#111827]">What Users Say</h2>
          <div className="h-1 w-16 bg-[#807aeb] mx-auto rounded-full" />
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          {[
            { name: "Priya Sharma", role: "Volunteer", text: "Vently helped me earn ₹15,000 in just 3 months! The platform is secure and the organizers are professional.", avatar: "👩‍💼" },
            { name: "Rajesh Kumar", role: "Event Organizer", text: "Finding reliable volunteers has never been easier. The verification system gives me confidence in my team.", avatar: "👨‍💼" },
            { name: "Ananya Patel", role: "Volunteer", text: "Great platform with transparent payments and fair treatment. I've made amazing connections here!", avatar: "👩‍🎓" },
          ].map((t, idx) => (
            <div key={idx} className="bg-white p-6 rounded-2xl border border-[#807aeb]/10 shadow-sm card-hover">
              <div className="flex items-center gap-3 mb-4">
                <span className="text-3xl">{t.avatar}</span>
                <div>
                  <p className="font-bold text-[#111827] text-sm">{t.name}</p>
                  <p className="text-xs text-[#6B7280]">{t.role}</p>
                </div>
              </div>
              <div className="flex gap-0.5 mb-3">
                {[...Array(5)].map((_, i) => <span key={i} className="text-yellow-400 text-sm">★</span>)}
              </div>
              <p className="text-[#6B7280] text-sm leading-relaxed italic">"{t.text}"</p>
            </div>
          ))}
        </div>
      </section>

      {/* CTA */}
      <section className="bg-[#807aeb] text-white py-20 px-6">
        <div className="max-w-4xl mx-auto text-center">
          <h2 className="text-4xl font-bold mb-4">Ready to Get Started?</h2>
          <p className="text-white/80 text-lg mb-8">Join thousands of volunteers and organizers on Vently today</p>
          <div className="flex gap-4 justify-center flex-wrap">
            <Link to="/register" className="bg-white text-[#807aeb] px-8 py-4 rounded-xl font-bold hover:shadow-lg hover:-translate-y-0.5">
              Sign Up as Volunteer
            </Link>
            <Link to="/register" className="border-2 border-white/40 text-white px-8 py-4 rounded-xl font-bold hover:bg-white/10">
              Sign Up as Organizer
            </Link>
          </div>
        </div>
      </section>

    </div>
  );
};

export default Home;
