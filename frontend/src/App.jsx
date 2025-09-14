import React, { useState, useEffect, useCallback } from 'react';
import { BrowserRouter as Router, Route, Routes, Link } from 'react-router-dom';
import axios from 'axios';
import Cart from './components/Cart';
import Profile from './components/Profile';
import AdminPanel from './components/AdminPanel';
import './App.css';

function App() {
  const [products, setProducts] = useState([]);
  const [cart, setCart] = useState([]);
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [isLoading, setIsLoading] = useState(false);
  const [token, setToken] = useState(localStorage.getItem('token') || '');
  const [theme, setTheme] = useState(localStorage.getItem('theme') || 'light');
  const [search, setSearch] = useState('');
  const [category, setCategory] = useState('');
  const [error, setError] = useState(null);
  const [showSignup, setShowSignup] = useState(false);
  const [showSignin, setShowSignin] = useState(false);
  const [showCart, setShowCart] = useState(false);
  const [toast, setToast] = useState({ message: '', type: '' });

  const loadProducts = useCallback(async () => {
    if (isLoading || page > totalPages) return;
    setIsLoading(true);
    setError(null);

    try {
      const params = new URLSearchParams({ page });
      if (search) params.append('search', search);
      if (category) params.append('category', category);
      const res = await axios.get(`/api/products?${params.toString()}`, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setProducts(prev => [...prev, ...res.data.products]);
      setTotalPages(res.data.totalPages);
      setPage(prev => prev + 1);
    } catch (e) {
      setError(`Failed to load products: ${e.message}. Check if backend is running at http://localhost:8080/shopease and DB is seeded.`);
    } finally {
      setIsLoading(false);
    }
  }, [page, isLoading, totalPages, token, search, category]);

  useEffect(() => {
    loadProducts();
  }, [loadProducts]);

  useEffect(() => {
    document.body.className = theme;
    localStorage.setItem('theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme(theme === 'light' ? 'dark' : 'light');
  };

  const handleSearch = () => {
    setProducts([]);
    setPage(1);
    loadProducts();
  };

  const addToCart = async (productId) => {
    try {
      await axios.post('/api/cart/add', { productId, quantity: 1 }, {
        headers: { Authorization: `Bearer ${token}` },
      });
      setToast({ message: 'Added to cart!', type: 'success' });
      loadCart();
    } catch (e) {
      setToast({ message: 'Failed to add to cart', type: 'error' });
    }
  };

  const loadCart = async () => {
    try {
      const res = await axios.get('/api/cart', {
        headers: { Authorization: `Bearer ${token}` },
      });
      setCart(res.data);
    } catch (e) {
      setToast({ message: 'Failed to load cart', type: 'error' });
    }
  };

  const checkout = async () => {
    try {
      const res = await axios.post('/api/payment/create-checkout-session', {}, {
        headers: { Authorization: `Bearer ${token}` },
      });
      window.location.href = `https://checkout.stripe.com/pay/${res.data.id}`;
    } catch (e) {
      setToast({ message: 'Checkout failed', type: 'error' });
    }
  };

  const handleSignup = async (e) => {
    e.preventDefault();
    const email = e.target.email.value;
    const password = e.target.password.value;
    const confirmPassword = e.target['confirm-password'].value;
    if (password !== confirmPassword) {
      setToast({ message: 'Passwords do not match', type: 'error' });
      return;
    }
    try {
      await axios.post('/api/auth/signup', { email, passwordHash: password });
      setToast({ message: 'Signup successful! Please sign in.', type: 'success' });
      setShowSignup(false);
      setShowSignin(true);
    } catch (e) {
      setToast({ message: e.response?.data?.error || 'Signup failed', type: 'error' });
    }
  };

  const handleSignin = async (e) => {
    e.preventDefault();
    const email = e.target.email.value;
    const password = e.target.password.value;
    try {
      const res = await axios.post('/api/auth/signin', { email, passwordHash: password });
      localStorage.setItem('token', res.data.token);
      setToken(res.data.token);
      setToast({ message: 'Signin successful', type: 'success' });
      setShowSignin(false);
      loadCart();
    } catch (e) {
      setToast({ message: e.response?.data?.error || 'Signin failed', type: 'error' });
    }
  };

  const signinWithGoogle = () => {
    window.location.href = '/api/auth/google';
  };

  return (
    <Router>
      <div className={`app ${theme}`}>
        <header className="header">
          <div className="container flex justify-between items-center">
            <Link to="/" className="logo">ShopEase</Link>
            <nav className="nav">
              <input
                type="text"
                placeholder="Search products..."
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                className="search-input"
              />
              <select value={category} onChange={(e) => setCategory(e.target.value)} className="category-select">
                <option value="">All Categories</option>
                <option value="electronics">Electronics</option>
                <option value="clothing">Clothing</option>
              </select>
              <button onClick={handleSearch} className="search-btn">Search</button>
              <button onClick={toggleTheme} className="theme-btn">{theme === 'light' ? 'Dark Mode' : 'Light Mode'}</button>
              <button onClick={() => setShowSignin(true)} className="auth-btn">Sign In</button>
              <button onClick={() => setShowSignup(true)} className="auth-btn">Sign Up</button>
              <button onClick={signinWithGoogle} className="google-btn">Google Sign In</button>
              <button onClick={() => setShowCart(true)} className="cart-btn">Cart ({cart.length})</button>
              <Link to="/profile" className="nav-link">Profile</Link>
              {token && <Link to="/admin" className="nav-link">Admin</Link>}
            </nav>
          </div>
        </header>

        <section className="hero">
          <video autoPlay muted loop className="hero-video">
            <source src="/demo-video.mp4" type="video/mp4" />
            Your browser does not support the video tag.
          </video>
          <div className="hero-content">
            <h1>Welcome to ShopEase</h1>
            <p>Discover premium products with effortless shopping</p>
            <button className="cta-btn">Shop Now</button>
          </div>
        </section>

        <section className="products container">
          {error && <div className="error-message">{error}</div>}
          <div className="grid">
            {products.map(p => (
              <div key={p.id} className="product-card">
                <img src={p.image || '/assets/placeholder.jpg'} alt={p.name} />
                <h2>{p.name}</h2>
                <p>{p.description}</p>
                <div className="price">${p.price.toFixed(2)}</div>
                <button onClick={() => addToCart(p.id)} className="add-to-cart">Add to Cart</button>
                <div className="reviews">4.5 ★ (123 reviews)</div>
              </div>
            ))}
          </div>
          {isLoading && <div className="loading">Loading...</div>}
        </section>

        {showSignup && (
          <div className="modal" onClick={() => setShowSignup(false)}>
            <div className="modal-content" onClick={e => e.stopPropagation()}>
              <h2 className="modal-title">Create Account</h2>
              <form onSubmit={handleSignup} className="modal-form">
                <input type="email" name="email" placeholder="Email" required />
                <input type="password" name="password" placeholder="Password" required minLength={6} />
                <input type="password" name="confirm-password" placeholder="Confirm Password" required minLength={6} />
                <button type="submit" className="modal-btn">Sign Up</button>
              </form>
              <button className="modal-close" onClick={() => setShowSignup(false)}>×</button>
            </div>
          </div>
        )}

        {showSignin && (
          <div className="modal" onClick={() => setShowSignin(false)}>
            <div className="modal-content" onClick={e => e.stopPropagation()}>
              <h2 className="modal-title">Sign In</h2>
              <form onSubmit={handleSignin} className="modal-form">
                <input type="email" name="email" placeholder="Email" required />
                <input type="password" name="password" placeholder="Password" required />
                <button type="submit" className="modal-btn">Sign In</button>
              </form>
              <button className="modal-close" onClick={() => setShowSignin(false)}>×</button>
            </div>
          </div>
        )}

        {showCart && (
          <div className="modal" onClick={() => setShowCart(false)}>
            <div className="modal-content" onClick={e => e.stopPropagation()}>
              <Cart cart={cart} onCheckout={checkout} />
              <button className="modal-close" onClick={() => setShowCart(false)}>×</button>
            </div>
          </div>
        )}

        {toast.message && (
          <div className={`toast ${toast.type}`}>
            {toast.message}
          </div>
        )}

        <Routes>
          <Route path="/" element={<></>} />
          <Route path="/profile" element={<Profile token={token} />} />
          <Route path="/admin" element={<AdminPanel token={token} />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
