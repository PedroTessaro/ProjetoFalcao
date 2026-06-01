// Polling do dashboard em tempo real. Em producao isso poderia ser um
// WebSocket/SSE empurrando os dados; aqui usamos polling simples e robusto.

function batteryClass(b) {
    if (b < 20) return 'danger';
    if (b < 50) return 'warn';
    return '';
}

function fmt(n, d = 1) {
    return (n === undefined || n === null) ? '—' : Number(n).toFixed(d);
}

function timeAgo(iso) {
    const s = Math.floor((Date.now() - new Date(iso).getTime()) / 1000);
    if (s < 60) return s + 's atrás';
    if (s < 3600) return Math.floor(s / 60) + 'min atrás';
    return Math.floor(s / 3600) + 'h atrás';
}

async function refresh() {
    let data;
    try {
        const res = await fetch('/api/dashboard', { headers: { 'Accept': 'application/json' } });
        if (!res.ok) return;
        data = await res.json();
    } catch (e) {
        return;
    }

    document.getElementById('kpi-total').textContent = data.fleet.total;
    document.getElementById('kpi-flight').textContent = data.fleet.inFlight;
    document.getElementById('kpi-missions').textContent = data.missions.inProgress;
    document.getElementById('kpi-priority').textContent = data.schedulingPriority;

    // Telemetria
    const tb = document.getElementById('telemetry-body');
    if (data.drones.length === 0) {
        tb.innerHTML = '<tr><td colspan="7" style="color:var(--muted)">Nenhum drone.</td></tr>';
    } else {
        tb.innerHTML = data.drones.map(d => {
            const bc = batteryClass(d.battery);
            return `<tr>
                <td class="mono">${d.codename}</td>
                <td><span class="badge ${d.status}">${d.status}</span></td>
                <td><div class="bar"><span class="${bc}" style="width:${d.battery}%"></span></div> ${d.battery}%</td>
                <td>${fmt(d.alt)}</td>
                <td>${fmt(d.speed)}</td>
                <td>${fmt(d.lidar)}</td>
                <td>${fmt(d.signal, 0)}</td>
            </tr>`;
        }).join('');
    }

    // Processos do SO
    const pb = document.getElementById('process-body');
    pb.innerHTML = data.processes.map(p => `<tr>
        <td class="mono">${p.name}</td>
        <td><strong>${p.priority}</strong></td>
        <td>${fmt(p.cpuPct)}</td>
        <td>${fmt(p.memMb)}</td>
        <td><span class="badge ${p.state === 'RUNNING' ? 'IN_FLIGHT' : 'IDLE'}">${p.state}</span></td>
    </tr>`).join('');

    // Ameacas
    const thb = document.getElementById('threat-body');
    if (!data.threats || data.threats.length === 0) {
        thb.innerHTML = '<tr><td colspan="6" style="color:var(--muted)">Sem ameaças registradas.</td></tr>';
    } else {
        thb.innerHTML = data.threats.map(t => `<tr>
            <td>${timeAgo(t.detectedAt)}</td>
            <td class="mono">${t.droneCodename}</td>
            <td>${t.type}</td>
            <td><span class="badge ${t.severity >= 7 ? 'CRITICAL' : 'HIGH'}">${t.severity}/10</span></td>
            <td>${fmt(t.distanceM)} m</td>
            <td class="mono">${t.evasiveAction}</td>
        </tr>`).join('');
    }
}

refresh();
setInterval(refresh, 2500);
