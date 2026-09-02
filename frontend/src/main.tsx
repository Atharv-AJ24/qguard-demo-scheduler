import { useEffect, useState } from 'react';
import { createRoot } from 'react-dom/client';
import axios from 'axios';
import './style.css';

const api = axios.create({
  baseURL:
    import.meta.env.VITE_API_URL ||
    'http://localhost:8080/api/v1',
});

type Slot = {
  startsAt: string;
  endsAt: string;
  display: string;
  available: boolean;
  bookedBy: string | null;
  bookedCompany: string | null;
};

const zone =
  Intl.DateTimeFormat().resolvedOptions().timeZone;

function Booking() {
  const [date, setDate] = useState(
    new Date().toISOString().slice(0, 10)
  );

  const [tz, setTz] = useState(zone);
  const [slots, setSlots] = useState<Slot[]>([]);
  const [slot, setSlot] = useState('');
  const [done, setDone] = useState('');
  const [error, setError] = useState('');

  useEffect(() => {
    setSlot('');
    setError('');

    api
      .get('/availability', {
        params: {
          date,
          timezone: tz,
        },
      })
      .then((r) => setSlots(r.data.slots))
      .catch(() =>
        setError('Unable to load availability.')
      );
  }, [date, tz]);

  async function submit(
    e: React.FormEvent<HTMLFormElement>
  ) {
    e.preventDefault();
    setError('');

    const selectedSlot = slots.find(
      (s) => s.startsAt === slot
    );

    if (!selectedSlot || !selectedSlot.available) {
      setError(
        selectedSlot?.bookedBy
          ? `This slot is already booked by ${selectedSlot.bookedBy}.`
          : 'That slot is no longer available.'
      );
      return;
    }

    const d = new FormData(e.currentTarget);

    try {
      const r = await api.post('/bookings', {
        name: d.get('name'),
        email: d.get('email'),
        company: d.get('company'),
        jobTitle: d.get('jobTitle'),
        phone: d.get('phone'),
        startsAt: slot,
        timezone: tz,
      });

      setDone(
        `Your demo is confirmed. A calendar invitation and secure management link were sent to ${r.data.booking.email}.`
      );
    } catch (e: any) {
      setError(
        e.response?.data?.message ||
          'Could not confirm your demo.'
      );

      // Refresh slots because another user may have
      // booked the selected slot.
      try {
        const r = await api.get('/availability', {
          params: {
            date,
            timezone: tz,
          },
        });

        setSlots(r.data.slots);
      } catch {
        // Keep the original booking error visible.
      }
    }
  }

  if (done) {
    return (
      <main>
        <h1>QGuard</h1>
        <h2>Demo confirmed</h2>
        <p>{done}</p>
      </main>
    );
  }

  return (
    <main>
      <header>
        <span>TrevasQ</span>
        <h1>Quantum-ready security starts here.</h1>
        <p>
          Schedule a 30-minute QGuard demo with our
          cybersecurity team.
        </p>
      </header>

      <form onSubmit={submit}>
        <h2>Schedule a demo</h2>

        <label>
          Your timezone
          <input
            value={tz}
            onChange={(e) => setTz(e.target.value)}
            required
          />
        </label>

        <label>
          Date
          <input
            type="date"
            value={date}
            min={new Date()
              .toISOString()
              .slice(0, 10)}
            onChange={(e) => setDate(e.target.value)}
            required
          />
        </label>

        <fieldset>
          <legend>Available times</legend>

          {slots.length ? (
            <div className="slots">
              {slots.map((s) => {
                const isSelected =
                  slot === s.startsAt;

                if (s.available) {
                  return (
                    <button
                      type="button"
                      className={
                        isSelected
                          ? 'selected'
                          : ''
                      }
                      onClick={() =>
                        setSlot(s.startsAt)
                      }
                      key={s.startsAt}
                    >
                      {s.display}
                    </button>
                  );
                }

                return (
                  <button
                    type="button"
                    className="booked"
                    disabled
                    key={s.startsAt}
                    title={
                      s.bookedBy
                        ? `Booked by ${s.bookedBy}${
                            s.bookedCompany
                              ? ` from ${s.bookedCompany}`
                              : ''
                          }`
                        : 'This slot is already booked'
                    }
                  >
                    {s.display} — Booked
                    {s.bookedBy && (
                      <small>
                        {' '}
                        ({s.bookedBy}
                        {s.bookedCompany
                          ? `, ${s.bookedCompany}`
                          : ''}
                        )
                      </small>
                    )}
                  </button>
                );
              })}
            </div>
          ) : (
            <p>No slots available.</p>
          )}
        </fieldset>

        <label>
          Name
          <input
            name="name"
            required
            maxLength={120}
          />
        </label>

        <label>
          Work email
          <input
            name="email"
            type="email"
            required
          />
        </label>

        <label>
          Company
          <input
            name="company"
            required
          />
        </label>

        <label>
          Job title
          <input
            name="jobTitle"
            required
          />
        </label>

        <label>
          Phone (optional)
          <input name="phone" />
        </label>

        {error && (
          <p className="error">{error}</p>
        )}

        <button disabled={!slot}>
          Confirm demo
        </button>
      </form>
    </main>
  );
}

function Manage() {
  const token =
    location.pathname.split('/').pop()!;

  const [b, setB] = useState<any>();
  const [msg, setMsg] = useState('');

  useEffect(() => {
    api
      .get('/bookings/manage/' + token)
      .then((r) => setB(r.data))
      .catch(() =>
        setMsg(
          'This management link is invalid or expired.'
        )
      );
  }, [token]);

  if (msg)
    return (
      <main>
        <p>{msg}</p>
      </main>
    );

  if (!b)
    return (
      <main>
        <p>Loading booking...</p>
      </main>
    );

  return (
    <main>
      <h1>Manage your QGuard demo</h1>

      <p>
        {new Date(b.startsAt).toLocaleString()} —{' '}
        {b.status}
      </p>

      {b.status === 'CONFIRMED' && (
        <button
          onClick={async () => {
            await api.post(
              '/bookings/manage/' +
                token +
                '/cancel'
            );

            setMsg(
              'Your demo was cancelled.'
            );
          }}
        >
          Cancel demo
        </button>
      )}

      {msg && <p>{msg}</p>}
    </main>
  );
}

createRoot(
  document.getElementById('root')!
).render(
  location.pathname.startsWith('/manage/')
    ? <Manage />
    : <Booking />
);