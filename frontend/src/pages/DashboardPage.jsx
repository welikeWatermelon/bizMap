import { useEffect, useRef } from 'react';
import Navbar from '../components/Navbar';
import useDashboard from '../hooks/useDashboard';
import { Chart, ArcElement, Tooltip, Legend } from 'chart.js';
import { Doughnut } from 'react-chartjs-2';

Chart.register(ArcElement, Tooltip, Legend);

export default function DashboardPage() {
  const { summary, loading, error, fetchSummary } = useDashboard();

  useEffect(() => { fetchSummary(); }, [fetchSummary]);

  if (loading) return <div><Navbar /><p style={{ padding: '24px' }}>로딩 중...</p></div>;
  if (error) return <div><Navbar /><p style={{ padding: '24px', color: '#d32f2f' }}>{error}</p></div>;
  if (!summary) return <div><Navbar /></div>;

  const chartData = {
    labels: summary.categoryStats.map((s) => categoryLabels[s.category] || s.category),
    datasets: [{
      data: summary.categoryStats.map((s) => s.count),
      backgroundColor: ['#1976d2', '#ff9800', '#4caf50', '#9c27b0'],
    }],
  };

  return (
    <div style={{ minHeight: '100vh', backgroundColor: '#f5f5f5' }}>
      <Navbar />
      <div style={styles.container}>
        <h2>대시보드</h2>

        <div style={styles.cards}>
          <div style={styles.statCard}>
            <span style={styles.statValue}>{summary.totalStores}</span>
            <span style={styles.statLabel}>전체 매장</span>
          </div>
          <div style={styles.statCard}>
            <span style={styles.statValue}>{summary.activeStores}</span>
            <span style={styles.statLabel}>활성 매장</span>
          </div>
          <div style={styles.statCard}>
            <span style={styles.statValue}>{summary.totalStores - summary.activeStores}</span>
            <span style={styles.statLabel}>비활성 매장</span>
          </div>
        </div>

        <div style={{ display: 'flex', gap: '24px', marginTop: '24px' }}>
          <div style={styles.chartCard}>
            <h3 style={{ margin: '0 0 16px' }}>카테고리별 매장</h3>
            {summary.categoryStats.length > 0 ? (
              <div style={{ maxWidth: '300px', margin: '0 auto' }}>
                <Doughnut data={chartData} />
              </div>
            ) : (
              <p style={{ color: '#999' }}>데이터가 없습니다.</p>
            )}
          </div>

          <div style={styles.chartCard}>
            <h3 style={{ margin: '0 0 16px' }}>최근 등록 매장</h3>
            {summary.recentStores.length > 0 ? (
              <table style={{ width: '100%', borderCollapse: 'collapse' }}>
                <thead>
                  <tr>
                    <th style={styles.th}>매장명</th>
                    <th style={styles.th}>등록일</th>
                  </tr>
                </thead>
                <tbody>
                  {summary.recentStores.map((s) => (
                    <tr key={s.id}>
                      <td style={styles.td}>{s.name}</td>
                      <td style={styles.td}>{s.createdAt?.substring(0, 10)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            ) : (
              <p style={{ color: '#999' }}>등록된 매장이 없습니다.</p>
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

const categoryLabels = { RETAIL: '소매', FOOD: '음식', SERVICE: '서비스', OTHER: '기타' };

const styles = {
  container: { maxWidth: '1000px', margin: '0 auto', padding: '24px' },
  cards: { display: 'flex', gap: '16px' },
  statCard: {
    flex: 1, backgroundColor: '#fff', padding: '24px', borderRadius: '8px',
    boxShadow: '0 1px 4px rgba(0,0,0,0.1)', textAlign: 'center',
    display: 'flex', flexDirection: 'column',
  },
  statValue: { fontSize: '36px', fontWeight: 'bold', color: '#1976d2' },
  statLabel: { fontSize: '14px', color: '#666', marginTop: '4px' },
  chartCard: {
    flex: 1, backgroundColor: '#fff', padding: '24px', borderRadius: '8px',
    boxShadow: '0 1px 4px rgba(0,0,0,0.1)',
  },
  th: { padding: '8px', borderBottom: '2px solid #e0e0e0', textAlign: 'left', fontSize: '13px', color: '#666' },
  td: { padding: '8px', borderBottom: '1px solid #f0f0f0', fontSize: '14px' },
};
