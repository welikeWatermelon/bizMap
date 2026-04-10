import { Link, useNavigate } from 'react-router-dom';

export default function Navbar() {
  const navigate = useNavigate();

  const handleLogout = () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    navigate('/login');
  };

  return (
    <nav style={styles.nav}>
      <div style={styles.brand}>
        <Link to="/map" style={styles.logo}>BizMap</Link>
      </div>
      <div style={styles.links}>
        <Link to="/find" style={styles.link}>매장 찾기</Link>
        <Link to="/map" style={styles.link}>지도</Link>
        <Link to="/stores" style={styles.link}>매장관리</Link>
        <Link to="/dashboard" style={styles.link}>대시보드</Link>
        <Link to="/widget" style={styles.link}>위젯 관리</Link>
        <button onClick={handleLogout} style={styles.logoutBtn}>로그아웃</button>
      </div>
    </nav>
  );
}

const styles = {
  nav: {
    display: 'flex', justifyContent: 'space-between', alignItems: 'center',
    padding: '0 24px', height: '56px', backgroundColor: '#1976d2', color: '#fff',
  },
  brand: { display: 'flex', alignItems: 'center' },
  logo: { color: '#fff', textDecoration: 'none', fontSize: '20px', fontWeight: 'bold' },
  links: { display: 'flex', alignItems: 'center', gap: '16px' },
  link: { color: '#fff', textDecoration: 'none', fontSize: '14px' },
  logoutBtn: {
    background: 'transparent', border: '1px solid #fff', color: '#fff',
    padding: '4px 12px', borderRadius: '4px', cursor: 'pointer', fontSize: '13px',
  },
};
