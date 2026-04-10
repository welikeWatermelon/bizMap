package com.bizmap.widget.controller;

import com.bizmap.common.response.ApiResponse;
import com.bizmap.widget.dto.WidgetStoreResponse;
import com.bizmap.widget.service.WidgetKeyService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/widget")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WidgetPublicController {

    private final WidgetKeyService widgetKeyService;

    @Value("${google.maps.api-key}")
    private String googleMapsApiKey;

    @GetMapping(value = "/bizmap-widget.js", produces = "application/javascript")
    public ResponseEntity<String> getWidgetScript(@RequestParam("key") String key) {
        widgetKeyService.findByApiKey(key);
        String js = buildWidgetJs(key, googleMapsApiKey);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/javascript"))
                .body(js);
    }

    @GetMapping("/api/stores")
    public ResponseEntity<ApiResponse<List<WidgetStoreResponse>>> getStores(@RequestParam("key") String key) {
        return ResponseEntity.ok(ApiResponse.success(widgetKeyService.getStoresByApiKey(key)));
    }

    @GetMapping("/api/stores/nearby")
    public ResponseEntity<ApiResponse<List<WidgetStoreResponse>>> getNearbyStores(
            @RequestParam("key") String key,
            @RequestParam double lat,
            @RequestParam double lng,
            @RequestParam(defaultValue = "5") double radius) {
        return ResponseEntity.ok(ApiResponse.success(
                widgetKeyService.getNearbyStoresByApiKey(key, lat, lng, radius)));
    }

    private String buildWidgetJs(String key, String mapsKey) {
        return """
                (function () {
                  var WIDGET_KEY = '%s';
                  var GMAPS_KEY = '%s';
                  var API_BASE = (function () {
                    try {
                      var script = document.currentScript;
                      if (script && script.src) {
                        var url = new URL(script.src);
                        return url.origin;
                      }
                    } catch (e) {}
                    return '';
                  })();

                  function ready(fn) {
                    if (document.readyState !== 'loading') fn();
                    else document.addEventListener('DOMContentLoaded', fn);
                  }

                  function ensureMapsLoaded(cb) {
                    if (window.google && window.google.maps) { cb(); return; }
                    window.__bizmapMapsCallbacks = window.__bizmapMapsCallbacks || [];
                    window.__bizmapMapsCallbacks.push(cb);
                    if (window.__bizmapMapsLoading) return;
                    window.__bizmapMapsLoading = true;
                    window.__bizmapInitMap = function () {
                      var queue = window.__bizmapMapsCallbacks || [];
                      window.__bizmapMapsCallbacks = [];
                      queue.forEach(function (fn) { try { fn(); } catch (e) { console.error('[BizMap]', e); } });
                    };
                    var script = document.createElement('script');
                    script.src = 'https://maps.googleapis.com/maps/api/js?key=' + GMAPS_KEY + '&callback=__bizmapInitMap&libraries=marker';
                    script.async = true;
                    script.defer = true;
                    script.onerror = function () {
                      console.error('[BizMap] Google Maps script failed to load');
                      var queue = window.__bizmapMapsCallbacks || [];
                      window.__bizmapMapsCallbacks = [];
                      queue.forEach(function (fn) { try { fn(); } catch (e) {} });
                    };
                    document.head.appendChild(script);
                  }

                  function loadStores(cb) {
                    var xhr = new XMLHttpRequest();
                    xhr.open('GET', API_BASE + '/widget/api/stores?key=' + WIDGET_KEY, true);
                    xhr.onload = function () {
                      if (xhr.status >= 200 && xhr.status < 300) {
                        try {
                          var res = JSON.parse(xhr.responseText);
                          cb(null, res.data || []);
                        } catch (e) { cb(e); }
                      } else {
                        cb(new Error('Failed to load stores: ' + xhr.status));
                      }
                    };
                    xhr.onerror = function () { cb(new Error('Network error')); };
                    xhr.send();
                  }

                  function renderFallback(container, stores) {
                    container.innerHTML = '';
                    var title = document.createElement('h3');
                    title.textContent = '매장 목록';
                    title.style.cssText = 'margin:0 0 12px 0;font-family:sans-serif;';
                    container.appendChild(title);
                    var ul = document.createElement('ul');
                    ul.style.cssText = 'list-style:none;padding:0;margin:0;font-family:sans-serif;font-size:14px;';
                    stores.forEach(function (s) {
                      var li = document.createElement('li');
                      li.style.cssText = 'padding:8px;border-bottom:1px solid #eee;';
                      li.innerHTML = '<strong>' + s.name + '</strong><br/>' +
                                     '<span style="color:#666;">' + (s.address || '') + '</span><br/>' +
                                     '<span style="color:#888;">' + (s.phone || '') + '</span>';
                      ul.appendChild(li);
                    });
                    container.appendChild(ul);
                  }

                  function renderMap(container, stores) {
                    if (!window.google || !window.google.maps) {
                      renderFallback(container, stores);
                      return;
                    }
                    container.innerHTML = '';
                    var center = stores.length > 0
                        ? { lat: stores[0].latitude, lng: stores[0].longitude }
                        : { lat: 37.5665, lng: 126.9780 };
                    var map = new google.maps.Map(container, {
                      center: center,
                      zoom: 12
                    });
                    var infoWindow = new google.maps.InfoWindow();
                    stores.forEach(function (s) {
                      if (s.latitude == null || s.longitude == null) return;
                      var marker = new google.maps.Marker({
                        position: { lat: s.latitude, lng: s.longitude },
                        map: map,
                        title: s.name
                      });
                      marker.addListener('click', function () {
                        infoWindow.setContent(
                          '<div style="font-family:sans-serif;font-size:13px;">' +
                          '<strong>' + s.name + '</strong><br/>' +
                          (s.address || '') + '<br/>' +
                          (s.phone || '') +
                          '</div>'
                        );
                        infoWindow.open(map, marker);
                      });
                    });
                  }

                  ready(function () {
                    var container = document.getElementById('bizmap-widget');
                    if (!container) {
                      console.error('[BizMap] #bizmap-widget element not found');
                      return;
                    }
                    container.style.border = container.style.border || '1px solid #ddd';
                    container.style.borderRadius = container.style.borderRadius || '8px';
                    container.style.overflow = 'hidden';
                    container.innerHTML = '<div style="padding:16px;font-family:sans-serif;color:#666;">매장 정보를 불러오는 중...</div>';

                    loadStores(function (err, stores) {
                      if (err) {
                        container.innerHTML = '<div style="padding:16px;font-family:sans-serif;color:#c00;">매장 정보를 불러오지 못했습니다.</div>';
                        console.error('[BizMap]', err);
                        return;
                      }
                      ensureMapsLoaded(function () {
                        renderMap(container, stores);
                      });
                    });
                  });
                })();
                """.formatted(key, mapsKey);
    }
}
