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

/* =========================================================
   NAVBAR
   ========================================================= */

function Navbar() {
  return (
    <nav className="navbar">
      <div className="nav-inner">
        <div className="brand">
          <div className="brand-mark">T</div>

          <span className="brand-name">
            TREVASQ
          </span>
        </div>

        <div className="nav-links">
          <a href="/">Home</a>

          <a
            href="https://trevasq.com"
            target="_blank"
            rel="noreferrer"
          >
            About
          </a>

          <a
            href="https://trevasq.com"
            target="_blank"
            rel="noreferrer"
          >
            Team
          </a>

          <a
            href="https://trevasq.com"
            target="_blank"
            rel="noreferrer"
          >
            Products
          </a>
        </div>
      </div>
    </nav>
  );
}

/* =========================================================
   BOOKING PAGE
   ========================================================= */

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

      try {
        const r = await api.get('/availability', {
          params: {
            date,
            timezone: tz,
          },
        });

        setSlots(r.data.slots);
      } catch {
        // Keep original booking error.
      }
    }
  }

  if (done) {
    return (
      <>
        <Navbar />

        <main className="page-shell">
          <section className="success-card">
            <div className="success-icon">
              ✓
            </div>

            <span className="eyebrow">
              TREVASQ · QGUARD
            </span>

            <h1>Demo confirmed</h1>

            <p>{done}</p>

            <a
              href="/"
              className="primary-button"
            >
              Back to QGuard
            </a>
          </section>
        </main>
      </>
    );
  }

  return (
    <>
      <Navbar />

      <main className="page-shell">
        <section className="hero">
          <span className="eyebrow">
            TREVASQ · QGUARD
          </span>

          <h1>
            Quantum-ready
            <br />
            security starts here.
          </h1>

          <p>
            Schedule a 30-minute QGuard demo with
            our cybersecurity team and discover how
            quantum-ready security protects your
            digital future.
          </p>
        </section>

        <section className="booking-card">
          <div className="booking-header">
            <div>
              <span className="section-label">
                QGUARD DEMO
              </span>

              <h2>Schedule a demo</h2>

              <p>
                Choose a convenient time and tell us
                a little about yourself.
              </p>
            </div>

            <div className="duration-badge">
              <span>◷</span>
              30 minutes
            </div>
          </div>

          <form onSubmit={submit}>
            <div className="booking-grid">
              {/* LEFT SIDE */}

              <div className="details-section">
                <div className="section-heading">
                  <span className="step-number">
                    01
                  </span>

                  <div>
                    <h3>Your details</h3>

                    <p>
                      Tell us who we'll be meeting.
                    </p>
                  </div>
                </div>

                <div className="field-grid">
                  <label className="full-field">
                    <span>Name</span>

                    <input
                      name="name"
                      placeholder="Your full name"
                      required
                      maxLength={120}
                    />
                  </label>

                  <label className="full-field">
                    <span>Work email</span>

                    <input
                      name="email"
                      type="email"
                      placeholder="you@company.com"
                      required
                      maxLength={254}
                      pattern="^[A-Za-z0-9.!#$%&'*+/=?^_`{|}~-]+@[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?(?:\.[A-Za-z0-9](?:[A-Za-z0-9-]{0,61}[A-Za-z0-9])?)+$"
                      title="Please enter a valid work email address, for example you@company.com"
                    />
                  </label>

                  <label>
                    <span>Company</span>

                    <input
                      name="company"
                      placeholder="Company name"
                      required
                    />
                  </label>

                  <label>
                    <span>Job title</span>

                    <input
                      name="jobTitle"
                      placeholder="Your role"
                      required
                    />
                  </label>

                  <label className="full-field">
                    <span>
                      Phone
                      <small>Optional</small>
                    </span>

                    <input
                      name="phone"
                      placeholder="+91..."
                    />
                  </label>
                </div>
              </div>

              {/* RIGHT SIDE */}

              <div className="schedule-section">
                <div className="section-heading">
                  <span className="step-number">
                    02
                  </span>

                  <div>
                    <h3>Choose your time</h3>

                    <p>
                      Select an available 30-minute
                      slot.
                    </p>
                  </div>
                </div>

                <div className="schedule-controls">
                  <label>
                    <span>Your timezone</span>

                    <input
                      value={tz}
                      onChange={(e) =>
                        setTz(e.target.value)
                      }
                      placeholder="Asia/Kolkata"
                      required
                    />
                  </label>

                  <label>
                    <span>Date</span>

                    <input
                      type="date"
                      value={date}
                      min={new Date()
                        .toISOString()
                        .slice(0, 10)}
                      onChange={(e) =>
                        setDate(e.target.value)
                      }
                      required
                    />
                  </label>
                </div>

                <fieldset>
                  <legend>
                    Available times
                  </legend>

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
                                  ? 'slot selected'
                                  : 'slot'
                              }
                              onClick={() =>
                                setSlot(
                                  s.startsAt
                                )
                              }
                              key={s.startsAt}
                            >
                              <strong>
                                {s.display}
                              </strong>

                              <small>
                                Available
                              </small>
                            </button>
                          );
                        }

                        return (
                          <button
                            type="button"
                            className="slot booked"
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
                            <strong>
                              {s.display}
                            </strong>

                            <small>
                              Booked
                            </small>

                            {s.bookedBy && (
                              <span className="booked-person">
                                {s.bookedBy}
                              </span>
                            )}

                            {s.bookedCompany && (
                              <span className="booked-company">
                                {s.bookedCompany}
                              </span>
                            )}
                          </button>
                        );
                      })}
                    </div>
                  ) : (
                    <p className="empty-slots">
                      No slots available for this
                      date.
                    </p>
                  )}
                </fieldset>
              </div>
            </div>

            {error && (
              <p className="error">
                {error}
              </p>
            )}

            <div className="form-footer">
              <div className="privacy-note">
                <span>🔒</span>

                <p>
                  Your information is securely
                  handled and used only for
                  scheduling your demo.
                </p>
              </div>

              <button
                className="primary-button"
                disabled={!slot}
              >
                Confirm demo
                <span>→</span>
              </button>
            </div>
          </form>
        </section>

        <section className="trust-strip">
          <div>
            <strong>QGuard</strong>

            <span>
              Quantum-ready cybersecurity
            </span>
          </div>

          <div>
            <strong>30 min</strong>

            <span>
              Live product walkthrough
            </span>
          </div>

          <div>
            <strong>Secure</strong>

            <span>
              Protected scheduling
            </span>
          </div>
        </section>
      </main>
    </>
  );
}

/* =========================================================
   MANAGE PAGE
   ========================================================= */

function Manage() {
  const token =
    location.pathname.split('/').pop()!;

  const [b, setB] = useState<any>();
  const [msg, setMsg] = useState('');
  const [error, setError] = useState('');

  const [rescheduling, setRescheduling] =
    useState(false);

  const [rescheduleDate, setRescheduleDate] =
    useState('');

  const [rescheduleTimezone, setRescheduleTimezone] =
    useState(zone);

  const [slots, setSlots] = useState<Slot[]>([]);
  const [selectedSlot, setSelectedSlot] =
    useState('');

  const [loadingSlots, setLoadingSlots] =
    useState(false);

  const [reschedulingSubmit, setReschedulingSubmit] =
    useState(false);

  useEffect(() => {
    api
      .get('/bookings/manage/' + token)
      .then((r) => {
        setB(r.data);

        setRescheduleTimezone(
          r.data.timezone || zone
        );

        setRescheduleDate(
          r.data.startsAt.slice(0, 10)
        );
      })
      .catch(() =>
        setMsg(
          'This management link is invalid or expired.'
        )
      );
  }, [token]);

  async function loadRescheduleSlots(
    date: string,
    timezone: string
  ) {
    if (!date || !timezone) return;

    setLoadingSlots(true);
    setError('');
    setSelectedSlot('');

    try {
      const r = await api.get('/availability', {
        params: {
          date,
          timezone,
        },
      });

      setSlots(r.data.slots);
    } catch {
      setError(
        'Unable to load available slots.'
      );

      setSlots([]);
    } finally {
      setLoadingSlots(false);
    }
  }

  async function openReschedule() {
    setRescheduling(true);

    await loadRescheduleSlots(
      rescheduleDate,
      rescheduleTimezone
    );
  }

  async function confirmReschedule() {
    setError('');
    setMsg('');

    if (!selectedSlot) {
      setError(
        'Please select a new time.'
      );
      return;
    }

    const selected = slots.find(
      (s) => s.startsAt === selectedSlot
    );

    if (!selected || !selected.available) {
      setError(
        selected?.bookedBy
          ? `This slot is already booked by ${selected.bookedBy}.`
          : 'That slot is no longer available.'
      );

      await loadRescheduleSlots(
        rescheduleDate,
        rescheduleTimezone
      );

      return;
    }

    setReschedulingSubmit(true);

    try {
      const r = await api.post(
        '/bookings/manage/' +
          token +
          '/reschedule',
        {
          startsAt: selectedSlot,
          timezone: rescheduleTimezone,
        }
      );

      setB(r.data);
      setRescheduling(false);
      setSelectedSlot('');
      setSlots([]);

      setMsg(
        'Your demo was rescheduled successfully. A confirmation email was sent.'
      );
    } catch (e: any) {
      setError(
        e.response?.data?.message ||
          'Could not reschedule your demo.'
      );

      await loadRescheduleSlots(
        rescheduleDate,
        rescheduleTimezone
      );
    } finally {
      setReschedulingSubmit(false);
    }
  }

  if (msg && !b) {
    return (
      <>
        <Navbar />

        <main className="page-shell">
          <section className="success-card">
            <h1>{msg}</h1>
          </section>
        </main>
      </>
    );
  }

  if (!b) {
    return (
      <>
        <Navbar />

        <main className="page-shell">
          <section className="loading-card">
            <p>Loading booking...</p>
          </section>
        </main>
      </>
    );
  }

  return (
    <>
      <Navbar />

      <main className="page-shell manage-page">
        <section className="manage-hero">
          <span className="eyebrow">
            QGUARD · DEMO MANAGEMENT
          </span>

          <h1>Manage your demo</h1>

          <p>
            Update or cancel your scheduled QGuard
            demonstration.
          </p>
        </section>

        {!rescheduling ? (
          <section className="manage-card">
            <div className="booking-status">
              <span className="status-dot" />

              <span>
                {b.status === 'CONFIRMED'
                  ? 'Demo confirmed'
                  : 'Demo cancelled'}
              </span>
            </div>

            <div className="appointment-info">
              <span className="info-label">
                Scheduled appointment
              </span>

              <h2>
                {new Date(
                  b.startsAt
                ).toLocaleString()}
              </h2>

              <p>
                {b.email}
                {b.company
                  ? ` · ${b.company}`
                  : ''}
              </p>
            </div>

            {msg && (
              <div className="success-message">
                ✓ {msg}
              </div>
            )}

            {error && (
              <p className="error">
                {error}
              </p>
            )}

            {b.status === 'CONFIRMED' && (
              <div className="manage-actions">
                <button
                  className="primary-button"
                  type="button"
                  onClick={openReschedule}
                >
                  Reschedule demo
                  <span>→</span>
                </button>

                <button
                  className="secondary-button"
                  type="button"
                  onClick={async () => {
                    try {
                      await api.post(
                        '/bookings/manage/' +
                          token +
                          '/cancel'
                      );

                      setB({
                        ...b,
                        status: 'CANCELLED',
                      });

                      setMsg(
                        'Your demo was cancelled.'
                      );
                    } catch {
                      setError(
                        'Could not cancel your demo.'
                      );
                    }
                  }}
                >
                  Cancel demo
                </button>
              </div>
            )}
          </section>
        ) : (
          <section className="manage-card">
            <div className="section-heading">
              <span className="step-number">
                01
              </span>

              <div>
                <h3>Choose a new time</h3>

                <p>
                  Select another available
                  30-minute slot.
                </p>
              </div>
            </div>

            <div className="schedule-controls">
              <label>
                <span>Your timezone</span>

                <input
                  value={rescheduleTimezone}
                  onChange={(e) =>
                    setRescheduleTimezone(
                      e.target.value
                    )
                  }
                  required
                />
              </label>

              <label>
                <span>New date</span>

                <input
                  type="date"
                  value={rescheduleDate}
                  min={new Date()
                    .toISOString()
                    .slice(0, 10)}
                  onChange={async (e) => {
                    const newDate =
                      e.target.value;

                    setRescheduleDate(
                      newDate
                    );

                    await loadRescheduleSlots(
                      newDate,
                      rescheduleTimezone
                    );
                  }}
                  required
                />
              </label>
            </div>

            <fieldset>
              <legend>
                Available times
              </legend>

              {loadingSlots ? (
                <p className="empty-slots">
                  Loading available slots...
                </p>
              ) : slots.length ? (
                <div className="slots">
                  {slots.map((s) => {
                    const isSelected =
                      selectedSlot ===
                      s.startsAt;

                    if (s.available) {
                      return (
                        <button
                          type="button"
                          className={
                            isSelected
                              ? 'slot selected'
                              : 'slot'
                          }
                          onClick={() =>
                            setSelectedSlot(
                              s.startsAt
                            )
                          }
                          key={s.startsAt}
                        >
                          <strong>
                            {s.display}
                          </strong>

                          <small>
                            Available
                          </small>
                        </button>
                      );
                    }

                    return (
                      <button
                        type="button"
                        className="slot booked"
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
                        <strong>
                          {s.display}
                        </strong>

                        <small>
                          Booked
                        </small>

                        {s.bookedBy && (
                          <span className="booked-person">
                            {s.bookedBy}
                          </span>
                        )}

                        {s.bookedCompany && (
                          <span className="booked-company">
                            {s.bookedCompany}
                          </span>
                        )}
                      </button>
                    );
                  })}
                </div>
              ) : (
                <p className="empty-slots">
                  No slots available for this
                  date.
                </p>
              )}
            </fieldset>

            {error && (
              <p className="error">
                {error}
              </p>
            )}

            <div className="manage-actions">
              <button
                className="primary-button"
                type="button"
                disabled={
                  !selectedSlot ||
                  reschedulingSubmit
                }
                onClick={confirmReschedule}
              >
                {reschedulingSubmit
                  ? 'Rescheduling...'
                  : 'Confirm reschedule'}

                {!reschedulingSubmit && (
                  <span>→</span>
                )}
              </button>

              <button
                className="secondary-button"
                type="button"
                onClick={() => {
                  setRescheduling(false);
                  setSelectedSlot('');
                  setSlots([]);
                  setError('');
                }}
              >
                Back
              </button>
            </div>
          </section>
        )}
      </main>
    </>
  );
}

/* =========================================================
   APP
   ========================================================= */

createRoot(
  document.getElementById('root')!
).render(
  location.pathname.startsWith('/manage/')
    ? <Manage />
    : <Booking />
);