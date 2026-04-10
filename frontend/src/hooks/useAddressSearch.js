import { useState, useRef, useCallback } from 'react';
import { importLibrary } from '@googlemaps/js-api-loader';

export default function useAddressSearch() {
  const [suggestions, setSuggestions] = useState([]);
  const [loading, setLoading] = useState(false);
  const debounceTimerRef = useRef(null);

  const search = useCallback(async (input) => {
    if (!input || input.length < 2) {
      setSuggestions([]);
      return;
    }

    setLoading(true);
    try {
      const { AutocompleteSuggestion } = await importLibrary('places');
      const { suggestions: results } = await AutocompleteSuggestion.fetchAutocompleteSuggestions({
        input,
        language: 'ko',
        region: 'kr',
      });

      setSuggestions(results.map((s) => ({
        placeId: s.placePrediction.placeId,
        description: s.placePrediction.text.toString(),
      })));
    } catch {
      setSuggestions([]);
    } finally {
      setLoading(false);
    }
  }, []);

  const debouncedSearch = useCallback((input) => {
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }
    debounceTimerRef.current = setTimeout(() => {
      search(input);
    }, 300);
  }, [search]);

  const selectPlace = useCallback(async (placeId) => {
    try {
      const { Place } = await importLibrary('places');
      const place = new Place({ id: placeId });
      await place.fetchFields({ fields: ['formattedAddress', 'location'] });

      setSuggestions([]);
      return {
        address: place.formattedAddress,
        lat: place.location.lat(),
        lng: place.location.lng(),
      };
    } catch {
      setSuggestions([]);
      return null;
    }
  }, []);

  const clearSuggestions = useCallback(() => {
    setSuggestions([]);
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }
  }, []);

  return { suggestions, loading, search: debouncedSearch, selectPlace, clearSuggestions };
}
