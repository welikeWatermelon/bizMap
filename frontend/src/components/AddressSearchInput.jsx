import { useState, useRef, useEffect } from 'react';
import useAddressSearch from '../hooks/useAddressSearch';

export default function AddressSearchInput({ onSelect, placeholder = '주소 검색', initialValue = '' }) {
  const [inputValue, setInputValue] = useState(initialValue);
  const [open, setOpen] = useState(false);
  const wrapperRef = useRef(null);
  const { suggestions, loading, search, selectPlace, clearSuggestions } = useAddressSearch();

  useEffect(() => {
    setInputValue(initialValue);
  }, [initialValue]);

  useEffect(() => {
    const handleClickOutside = (e) => {
      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {
        setOpen(false);
        clearSuggestions();
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [clearSuggestions]);

  const handleChange = (e) => {
    const value = e.target.value;
    setInputValue(value);
    if (value.length >= 2) {
      setOpen(true);
      search(value);
    } else {
      setOpen(false);
      clearSuggestions();
    }
  };

  const handleSelect = async (suggestion) => {
    const result = await selectPlace(suggestion.placeId);
    if (result) {
      setInputValue(result.address);
      onSelect(result);
    }
    setOpen(false);
  };

  return (
    <div ref={wrapperRef} style={styles.wrapper}>
      <input
        type="text"
        value={inputValue}
        onChange={handleChange}
        placeholder={placeholder}
        style={styles.input}
        onFocus={() => { if (suggestions.length > 0) setOpen(true); }}
      />
      {open && (
        <div style={styles.dropdown}>
          {loading && (
            <div style={styles.item}>검색 중...</div>
          )}
          {!loading && suggestions.length === 0 && inputValue.length >= 2 && (
            <div style={styles.item}>검색 결과 없음</div>
          )}
          {suggestions.map((s) => (
            <div
              key={s.placeId}
              style={styles.item}
              onMouseDown={() => handleSelect(s)}
              onMouseEnter={(e) => { e.currentTarget.style.backgroundColor = '#f0f0f0'; }}
              onMouseLeave={(e) => { e.currentTarget.style.backgroundColor = '#fff'; }}
            >
              <div style={{ fontSize: '14px' }}>{s.description}</div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}

const styles = {
  wrapper: { position: 'relative', width: '100%' },
  input: {
    width: '100%', padding: '8px', border: '1px solid #ddd',
    borderRadius: '4px', fontSize: '14px', boxSizing: 'border-box',
  },
  dropdown: {
    position: 'absolute', top: '100%', left: 0, right: 0,
    backgroundColor: '#fff', border: '1px solid #ddd', borderTop: 'none',
    borderRadius: '0 0 4px 4px', maxHeight: '240px', overflowY: 'auto',
    zIndex: 1000, boxShadow: '0 4px 6px rgba(0,0,0,0.1)',
  },
  item: {
    padding: '10px 12px', cursor: 'pointer', borderBottom: '1px solid #f0f0f0',
  },
};
