import React from "react";
import { Link } from "react-router-dom";

const AuthLayout = ({ children, title, subtitle }) => (
  <div className="min-h-screen bg-[#ebf2fa] flex flex-col md:flex-row">
    {/* Left Side */}
    <div className="hidden md:flex md:w-1/2 bg-[#807aeb] p-12 flex-col justify-between text-white relative overflow-hidden">
      {/* Decorative circles */}
      <div className="absolute top-0 right-0 w-64 h-64 bg-white/10 rounded-full -mr-32 -mt-32" />
      <div className="absolute bottom-0 left-0 w-48 h-48 bg-white/5 rounded-full -ml-24 -mb-24" />

      <div className="relative z-10">
        <h1 className="text-2xl font-black tracking-tighter">Vently<span className="text-white/60">.</span></h1>
      </div>
      <div className="relative z-10">
        <h2 className="text-5xl font-bold leading-tight mb-6">
          {title}<br />
          <span className="text-white/80">Vently</span>
        </h2>
        <p className="text-white/70 text-lg max-w-md leading-relaxed">{subtitle}</p>

        <div className="mt-10 flex flex-col gap-3">
          {["Verified volunteers & organizers", "Secure UPI payments", "Real-time notifications"].map((item, i) => (
            <div key={i} className="flex items-center gap-3 text-white/80 text-sm">
              <span className="w-5 h-5 bg-white/20 rounded-full flex items-center justify-center text-xs">✓</span>
              {item}
            </div>
          ))}
        </div>
      </div>
      <div className="relative z-10 text-white/40 text-sm">© 2026 Vently</div>
    </div>

    {/* Right Side: Form */}
    <div className="w-full md:w-1/2 flex items-center justify-center p-8 bg-[#ebf2fa]">
      <div className="w-full max-w-md">
        <Link
          to="/"
          className="inline-flex items-center gap-1.5 text-sm text-[#6B7280] hover:text-[#807aeb] transition mb-6"
        >
          ← Back to Home
        </Link>
        <div className="bg-white rounded-2xl p-8 shadow-sm border border-[#807aeb]/10 animate-scale-in">
          {children}
        </div>
      </div>
    </div>
  </div>
);

export default AuthLayout;
