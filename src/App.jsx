import { useEffect, useRef, useState } from "react";
import productimg from "./assets/product.jpg";

console.log("C:/Users/Pranshu/Desktop/ClickPulseFrontend/clickpulse-ui/src/assets/product.jpg")
const API = import.meta.env.VITE_API_BASE ?? "/api";
+console.log("API base =", API);
// --- API helpers ---
async function postAction(action, qty = 1) {
  await fetch(`${API}/action`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ action, n: qty, t: Date.now() })
  });
}
async function getStats() {
  const r = await fetch(`${API}/stats`);
  if (!r.ok) throw new Error("stats failed");
  return r.json(); // { total:[{action|tag,value}], rate:[{action|tag,value}], ts }
}
// normalize server items that might use `action` or `tag`
const keyOf = (item) => item?.action ?? item?.tag ?? "unknown";
const pick = (arr, name) => arr.find(s => keyOf(s) === name)?.value ?? 0;

export default function App() {
  // UI state
  const [qty, setQty] = useState(1);
  const [stats, setStats] = useState({ total: [], rate: [] });
  const [msg, setMsg] = useState("");
  const [auto, setAuto] = useState(false);
  const autoTimer = useRef(null);

  // Derived stats
  const totalATC = pick(stats.total, "add_to_cart");
  const totalBUY = pick(stats.total, "buy_now");
  const rateATC = pick(stats.rate, "add_to_cart");
  const rateBUY = pick(stats.rate, "buy_now");
  const totalAll = totalATC + totalBUY;

  // Poll stats every 3s
  useEffect(() => {
    const poll = async () => { try { setStats(await getStats()); } catch { } };
    poll(); const id = setInterval(poll, 3000);
    return () => clearInterval(id);
  }, []);

  // Auto demo: alternate actions every 500ms
  useEffect(() => {
    if (!auto) { if (autoTimer.current) clearInterval(autoTimer.current); return; }
    let flip = false;
    autoTimer.current = setInterval(() => {
      flip = !flip;
      postAction(flip ? "add_to_cart" : "buy_now").catch(() => { });
    }, 500);
    return () => { if (autoTimer.current) clearInterval(autoTimer.current); };
  }, [auto]);

  const act = async (action) => {
    setMsg("Working…");
    try {
      await postAction(action, Math.max(1, Number(qty) || 1));
      setMsg(action === "add_to_cart" ? "Added to cart!" : "Order placed!");
    } catch {
      setMsg("Something went wrong.");
    } finally {
      setTimeout(() => setMsg(""), 1200);
    }
  };

  // tiny UI helpers
  const Pill = ({ children }) => (
    <span style={{ background: "#eef", borderRadius: 999, padding: "4px 10px", fontSize: 12 }}>{children}</span>
  );
  const Stat = ({ label, value, sub }) => (
    <div style={{ border: "1px solid #eee", borderRadius: 12, padding: 14 }}>
      <div style={{ fontSize: 12, color: "#777" }}>{label}</div>
      <div style={{ fontSize: 28, fontWeight: 700 }}>{value}</div>
      {sub ? <div style={{ fontSize: 12, color: "#777" }}>{sub}</div> : null}
    </div>
  );

  return (
    <div style={{ fontFamily: "system-ui, -apple-system, Segoe UI, Roboto, Arial", background: "#fafafa", minHeight: "100vh" }}>
      <header style={{ padding: "18px 20px", borderBottom: "1px solid #eee", background: "#fff", position: "sticky", top: 0, zIndex: 1 }}>
        <div style={{ maxWidth: 1100, margin: "0 auto", display: "flex", justifyContent: "space-between", alignItems: "center" }}>
          <strong style={{ fontSize: 18 }}>ClickPulse Store</strong>
          <div style={{ display: "flex", gap: 10, alignItems: "center" }}>
            <Pill>Total actions: {totalAll}</Pill>
            <label style={{ display: "flex", alignItems: "center", gap: 6, fontSize: 13 }}>
              <input type="checkbox" checked={auto} onChange={e => setAuto(e.target.checked)} />
              Demo mode
            </label>
          </div>
        </div>
      </header>

      <main style={{ maxWidth: 1100, margin: "24px auto", padding: "0 16px" }}>
        <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 24 }}>
          {/* Product card */}
          <div style={{ background: "#fff", border: "1px solid #eee", borderRadius: 16, padding: 18 }}>
            <img src={productimg} alt="Product" style={{ width: "100%", borderRadius: 12, objectFit: "cover" }} />
            <h2 style={{ margin: "14px 0 6px" }}>Wireless Noise‑Canceling Headphones</h2>
            <div style={{ color: "#666", fontSize: 14, marginBottom: 8 }}>
              Immersive sound, 30-hour battery, BT 5.3, USB‑C fast charge.
            </div>
            <div style={{ display: "flex", alignItems: "baseline", gap: 10, margin: "10px 0 16px" }}>
              <div style={{ fontSize: 26, fontWeight: 800 }}>$149</div>
              <div style={{ textDecoration: "line-through", color: "#999" }}>$199</div>
              <Pill>25% OFF</Pill>
            </div>

            <div style={{ display: "flex", gap: 12, alignItems: "center", marginBottom: 14 }}>
              <label style={{ fontSize: 14, color: "#555" }}>
                Qty
                <input
                  type="number"
                  min={1}
                  value={qty}
                  onChange={e => setQty(e.target.value)}
                  style={{ width: 70, marginLeft: 8, padding: "6px 8px", border: "1px solid #ddd", borderRadius: 8 }}
                />
              </label>
              {msg && <span style={{ color: "#0a7", fontSize: 13 }}>{msg}</span>}
            </div>

            <div style={{ display: "flex", gap: 12, flexWrap: "wrap" }}>
              <button
                onClick={() => act("add_to_cart")}
                style={{ padding: "10px 14px", color: "black", borderRadius: 10, border: "1px solid #ddd", background: "#fff", cursor: "pointer" }}
              >
                🛒 Add to cart
              </button>
              <button
                onClick={() => act("buy_now")}
                style={{ padding: "10px 14px", borderRadius: 10, border: "1px solid #0a7", background: "#0a7", color: "#fff", cursor: "pointer" }}
              >
                ⚡ Buy now
              </button>
            </div>
          </div>

          {/* Live metrics */}
          <div style={{ background: "#fff", border: "1px solid #eee", borderRadius: 16, padding: 18 }}>
            <h3 style={{ marginTop: 0 }}>Live actions</h3>
            <div style={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 12 }}>
              <Stat label="Add to cart — total" value={Math.floor(totalATC)} sub={`rate: ${rateATC.toFixed(2)}/s`} />
              <Stat label="Buy now — total" value={Math.floor(totalBUY)} sub={`rate: ${rateBUY.toFixed(2)}/s`} />
              <Stat label="All actions" value={Math.floor(totalAll)} />
              <Stat
                label="Leader"
                value={
                  totalATC === totalBUY ? "Tie"
                    : (totalATC > totalBUY ? `Add to cart +${Math.floor(totalATC - totalBUY)}`
                      : `Buy now +${Math.floor(totalBUY - totalATC)}`)
                }
                sub="cumulative"
              />
            </div>

            <div style={{ marginTop: 16, fontSize: 12, color: "#666" }}>
              Data source: Prometheus via Spring `/api/stats` (Micrometer metric <code>clickpulse_actions_total</code> grouped by <code>action</code>).
            </div>
          </div>
        </div>
      </main>

      <footer style={{ textAlign: "center", color: "#999", fontSize: 12, padding: "18px 0" }}>
        © {new Date().getFullYear()} ClickPulse demo
      </footer>
    </div>
  );
}
