import React, { useEffect, useState, startTransition } from 'react';
import { createRoot } from 'react-dom/client';
import QRCode from 'qrcode';
import './styles.css';

const API = 'http://localhost:8080/api';

function App() {
  const [token, setToken] = useState(localStorage.getItem('authToken') || '');
  const [me, setMe] = useState(null);
  const [challengeId, setChallengeId] = useState('');
  const [login, setLogin] = useState({ email: 'admin@demo.rs', password: 'password', code: '' });
  const [dashboard, setDashboard] = useState({ matches: [], courts: [], reservations: [], rankings: [], tournaments: [], notifications: [], players: [] });
  const [matchForm, setMatchForm] = useState({ title: '', startTime: '', playerAId: '', playerBId: '', courtId: '' });
  const [playerForm, setPlayerForm] = useState({ displayName: '', email: '', password: 'password' });
  const [message, setMessage] = useState('');
  const [twoFactorQr, setTwoFactorQr] = useState('');
  const headers = token ? { 'X-Auth-Token': token } : {};

  useEffect(() => {
    if (!token) {
      return;
    }
    loadDashboard();
  }, [token]);

  async function request(path, options = {}) {
    const response = await fetch(`${API}${path}`, {
      ...options,
      headers: {
        'Content-Type': 'application/json',
        ...headers,
        ...options.headers
      }
    });
    if (!response.ok) {
      throw new Error(await response.text());
    }
    return response.status === 204 ? null : response.json();
  }

  async function loadDashboard() {
    try {
      const [profile, matches, courts, reservations, rankings, tournaments, notifications, players] = await Promise.all([
        request('/auth/me'),
        request('/matches'),
        request('/courts'),
        request('/courts/reservations'),
        request('/rankings'),
        request('/tournaments'),
        request('/profile/notifications'),
        request('/players')
      ]);
      startTransition(() => {
        setMe(profile);
        setDashboard({ matches, courts, reservations, rankings, tournaments, notifications, players });
        setMatchForm((current) => ({
          ...current,
          playerAId: current.playerAId || String(profile.accountType === 'PLAYER' ? profile.id : players[0]?.id || ''),
          playerBId: current.playerBId || String(players.find((player) => player.id !== profile.id)?.id || ''),
          courtId: current.courtId || String(courts[0]?.id || '')
        }));
      });
    } catch (error) {
      setMessage(error.message);
    }
  }

  async function submitLogin(event) {
    event.preventDefault();
    setMessage('');
    try {
      const result = await request('/auth/login', {
        method: 'POST',
        body: JSON.stringify({ email: login.email, password: login.password })
      });
      if (result.requiresTwoFactor) {
        setChallengeId(result.challengeId);
        return;
      }
      localStorage.setItem('authToken', result.token);
      setToken(result.token);
    } catch (error) {
      setMessage(error.message);
    }
  }

  async function verifyTwoFactor(event) {
    event.preventDefault();
    const result = await request('/auth/2fa/verify', {
      method: 'POST',
      body: JSON.stringify({ challengeId, code: login.code })
    });
    localStorage.setItem('authToken', result.token);
    setToken(result.token);
    setChallengeId('');
  }

  async function enableTwoFactor() {
    try {
      const setup = await request('/auth/2fa/enable', { method: 'POST' });
      const qr = await QRCode.toDataURL(setup.provisioningUri, { margin: 1, width: 220 });
      setTwoFactorQr(qr);
      setMessage('2FA je aktiviran. Skeniraj QR kod u authenticator aplikaciji pre odjave.');
      loadDashboard();
    } catch (error) {
      setMessage(error.message);
    }
  }

  async function createMatch(event) {
    event.preventDefault();
    setMessage('');
    try {
      await request('/matches', {
        method: 'POST',
        body: JSON.stringify({
          title: matchForm.title,
          startTime: matchForm.startTime,
          playerAId: Number(matchForm.playerAId),
          playerBId: matchForm.playerBId ? Number(matchForm.playerBId) : null,
          courtId: Number(matchForm.courtId)
        })
      });
      setMatchForm({ title: '', startTime: '', playerAId: matchForm.playerAId, playerBId: matchForm.playerBId, courtId: matchForm.courtId });
      setMessage('Mec je dodat.');
      loadDashboard();
    } catch (error) {
      setMessage(error.message);
    }
  }

  async function createPlayer(event) {
    event.preventDefault();
    setMessage('');
    try {
      const player = await request('/auth/register', {
        method: 'POST',
        body: JSON.stringify({
          email: playerForm.email,
          displayName: playerForm.displayName,
          password: playerForm.password,
          accountType: 'PLAYER'
        })
      });
      setPlayerForm({ displayName: '', email: '', password: 'password' });
      setMatchForm((current) => ({
        ...current,
        playerBId: current.playerBId || String(player.id)
      }));
      setMessage('Igrac je dodat i dostupan je za izbor u mecu.');
      loadDashboard();
    } catch (error) {
      setMessage(error.message);
    }
  }

  function logout() {
    localStorage.removeItem('authToken');
    setToken('');
    setMe(null);
  }

  if (!token) {
    return (
      <main className="login-shell">
        <section className="login-card">
          <p className="eyebrow">Java + React MVP</p>
          <h1>Rekreativni mecevi, tereni i rang liste.</h1>
          <p className="muted">Demo nalozi: admin@demo.rs, ana@demo.rs, milos@demo.rs, club@demo.rs. Lozinka je password.</p>
          <form onSubmit={challengeId ? verifyTwoFactor : submitLogin}>
            <label>
              Email
              <input value={login.email} onChange={(event) => setLogin({ ...login, email: event.target.value })} />
            </label>
            {!challengeId && (
              <label>
                Lozinka
                <input type="password" value={login.password} onChange={(event) => setLogin({ ...login, password: event.target.value })} />
              </label>
            )}
            {challengeId && (
              <label>
                2FA kod
                <input value={login.code} onChange={(event) => setLogin({ ...login, code: event.target.value })} placeholder="123456" />
              </label>
            )}
            <button>{challengeId ? 'Potvrdi 2FA' : 'Uloguj se'}</button>
          </form>
          {message && <p className="message">{message}</p>}
        </section>
      </main>
    );
  }

  return (
    <main className="app-shell">
      <header className="hero">
        <div>
          <p className="eyebrow">Sportski operativni panel</p>
          <h1>Mecevi, tereni, turniri i export na jednom mestu.</h1>
          <p>Ulogovan: {me?.displayName} ({me?.accountType})</p>
        </div>
        <div className="actions">
          <a href={`${API}/export/matches.csv`}>CSV mecevi</a>
          <a href={`${API}/export/rankings.pdf`}>PDF rang lista</a>
          <button onClick={enableTwoFactor}>Aktiviraj 2FA</button>
          <button onClick={logout}>Odjava</button>
        </div>
      </header>

      {message && <section className="notice">{message}</section>}

      {twoFactorQr && (
        <section className="two-factor-setup">
          <div>
            <p className="eyebrow">2FA setup</p>
            <h2>Skeniraj QR kod</h2>
            <p className="muted">Osetljivi setup podaci se ne prikazuju kao tekst. Koristi Google Authenticator, Microsoft Authenticator ili sličnu aplikaciju.</p>
          </div>
          <img src={twoFactorQr} alt="QR kod za 2FA aktivaciju" />
          <button onClick={() => setTwoFactorQr('')}>Zatvori</button>
        </section>
      )}

      <section className="form-band">
        <div>
          <p className="eyebrow">Novi igrac</p>
          <h2>Dodaj ucesnika</h2>
        </div>
        <form className="player-form" onSubmit={createPlayer}>
          <label>
            Ime
            <input
              required
              value={playerForm.displayName}
              onChange={(event) => setPlayerForm({ ...playerForm, displayName: event.target.value })}
              placeholder="Petar Petrovic"
            />
          </label>
          <label>
            Email
            <input
              required
              type="email"
              value={playerForm.email}
              onChange={(event) => setPlayerForm({ ...playerForm, email: event.target.value })}
              placeholder="petar@demo.rs"
            />
          </label>
          <label>
            Lozinka
            <input
              required
              type="password"
              value={playerForm.password}
              onChange={(event) => setPlayerForm({ ...playerForm, password: event.target.value })}
            />
          </label>
          <button>Dodaj igraca</button>
        </form>
      </section>

      <section className="form-band">
        <div>
          <p className="eyebrow">Novi mec</p>
          <h2>Dodaj mec i izaberi ucesnike</h2>
        </div>
        <form className="match-form" onSubmit={createMatch}>
          <label>
            Naziv
            <input
              required
              value={matchForm.title}
              onChange={(event) => setMatchForm({ ...matchForm, title: event.target.value })}
              placeholder="Ana vs Milos"
            />
          </label>
          <label>
            Termin
            <input
              required
              type="datetime-local"
              value={matchForm.startTime}
              onChange={(event) => setMatchForm({ ...matchForm, startTime: event.target.value })}
            />
          </label>
          <label>
            Igrac A
            <select required value={matchForm.playerAId} onChange={(event) => setMatchForm({ ...matchForm, playerAId: event.target.value })}>
              <option value="">Izaberi igraca</option>
              {dashboard.players.map((player) => (
                <option key={player.id} value={player.id}>{player.displayName}</option>
              ))}
            </select>
          </label>
          <label>
            Igrac B
            <select value={matchForm.playerBId} onChange={(event) => setMatchForm({ ...matchForm, playerBId: event.target.value })}>
              <option value="">Otvoren mec</option>
              {dashboard.players
                .filter((player) => String(player.id) !== matchForm.playerAId)
                .map((player) => (
                  <option key={player.id} value={player.id}>{player.displayName}</option>
                ))}
            </select>
          </label>
          <label>
            Teren
            <select required value={matchForm.courtId} onChange={(event) => setMatchForm({ ...matchForm, courtId: event.target.value })}>
              <option value="">Izaberi teren</option>
              {dashboard.courts.map((court) => (
                <option key={court.id} value={court.id}>{court.name} · {court.location}</option>
              ))}
            </select>
          </label>
          <button>Dodaj mec</button>
        </form>
      </section>

      <section className="grid">
        <Panel title="Mecevi" kicker="prethodni, danasnji, predstojeci">
          {dashboard.matches.map((match) => (
            <Card key={match.id} title={match.title} meta={`${date(match.startTime)} · ${match.status}`}>
              <span>{match.playerA?.displayName || '-'} vs {match.playerB?.displayName || '-'}</span>
              <strong>{match.score || 'rezultat nije unet'}</strong>
            </Card>
          ))}
        </Panel>

        <Panel title="Tereni" kicker="rezervacije i zauzetost">
          {dashboard.courts.map((court) => (
            <Card key={court.id} title={court.name} meta={`${court.surface} · ${court.location}`}>
              <span>{court.club?.displayName}</span>
            </Card>
          ))}
        </Panel>

        <Panel title="Rang lista" kicker="W/L odnos">
          {dashboard.rankings.map((ranking, index) => (
            <Card key={ranking.playerId} title={`${index + 1}. ${ranking.displayName}`} meta={`${ranking.wins}W / ${ranking.losses}L`}>
              <span>Odigrano: {ranking.played}</span>
            </Card>
          ))}
        </Panel>

        <Panel title="Turniri" kicker="naziv, opis, mesto">
          {dashboard.tournaments.map((tournament) => (
            <Card key={tournament.id} title={tournament.name} meta={`${tournament.city} · ${tournament.maxPlayers} igraca`}>
              <span>{date(tournament.startsOn)} - {date(tournament.endsOn)}</span>
            </Card>
          ))}
        </Panel>

        <Panel title="Rezervacije" kicker="pregled zauzetosti">
          {dashboard.reservations.map((reservation) => (
            <Card key={reservation.id} title={reservation.court.name} meta={reservation.status}>
              <span>{date(reservation.startsAt)} - {date(reservation.endsAt)}</span>
            </Card>
          ))}
        </Panel>

        <Panel title="Obavestenja" kicker="aktivnosti naloga">
          {dashboard.notifications.map((notification) => (
            <Card key={notification.id} title={notification.message} meta={date(notification.createdAt)}>
              <span>{notification.read ? 'procitano' : 'novo'}</span>
            </Card>
          ))}
        </Panel>
      </section>
    </main>
  );
}

function Panel({ title, kicker, children }) {
  return (
    <section className="panel">
      <p className="eyebrow">{kicker}</p>
      <h2>{title}</h2>
      <div className="stack">{children}</div>
    </section>
  );
}

function Card({ title, meta, children }) {
  return (
    <article className="card">
      <div>
        <h3>{title}</h3>
        <p>{meta}</p>
      </div>
      {children}
    </article>
  );
}

function date(value) {
  if (!value) {
    return '-';
  }
  return new Intl.DateTimeFormat('sr-RS', { dateStyle: 'medium', timeStyle: value.includes('T') ? 'short' : undefined }).format(new Date(value));
}

createRoot(document.getElementById('root')).render(<App />);
