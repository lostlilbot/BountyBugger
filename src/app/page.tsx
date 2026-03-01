import Link from "next/link";

export default function Home() {
  return (
    <main className="min-h-screen bg-neutral-900 text-white">
      <div className="max-w-4xl mx-auto px-4 py-16">
        <header className="text-center mb-16">
          <h1 className="text-5xl font-bold mb-4 text-emerald-400">BountyBugger</h1>
          <p className="text-xl text-neutral-400">Your Mobile Bug Bounty Companion</p>
        </header>

        <section className="grid md:grid-cols-2 gap-6 mb-16">
          <div className="bg-neutral-800 rounded-xl p-6 border border-neutral-700">
            <h2 className="text-2xl font-semibold mb-3 text-emerald-400">Web Scanner</h2>
            <p className="text-neutral-400 mb-4">
              Scan websites for common vulnerabilities including SQL injection, XSS, and more.
            </p>
            <div className="text-sm text-neutral-500">
              Features: URL scanning, vulnerability detection, detailed reports
            </div>
          </div>

          <div className="bg-neutral-800 rounded-xl p-6 border border-neutral-700">
            <h2 className="text-2xl font-semibold mb-3 text-emerald-400">Network Scanner</h2>
            <p className="text-neutral-400 mb-4">
              Discover open ports and services on local networks.
            </p>
            <div className="text-sm text-neutral-500">
              Features: Port scanning, service detection, network mapping
            </div>
          </div>

          <div className="bg-neutral-800 rounded-xl p-6 border border-neutral-700">
            <h2 className="text-2xl font-semibold mb-3 text-emerald-400">Mobile Analysis</h2>
            <p className="text-neutral-400 mb-4">
              Analyze Android APKs for security issues.
            </p>
            <div className="text-sm text-neutral-500">
              Features: APK inspection, permission analysis, security assessment
            </div>
          </div>

          <div className="bg-neutral-800 rounded-xl p-6 border border-neutral-700">
            <h2 className="text-2xl font-semibold mb-3 text-emerald-400">Tool Manager</h2>
            <p className="text-neutral-400 mb-4">
              Download and manage security tools for bug bounty hunting.
            </p>
            <div className="text-sm text-neutral-500">
              Features: Tool catalog, download management, easy installation
            </div>
          </div>
        </section>

        <section className="text-center">
          <h2 className="text-2xl font-semibold mb-4 text-emerald-400">Download the App</h2>
          <p className="text-neutral-400 mb-6">
            Get BountyBugger on your Android device to start hunting for bugs.
          </p>
          <div className="inline-block bg-emerald-600 hover:bg-emerald-500 text-white font-semibold py-3 px-8 rounded-lg transition-colors">
            <a href="/BountyBugger-debug.apk" download>
              Download APK
            </a>
          </div>
        </section>
      </div>
    </main>
  );
}
