"use strict";

var _typeof = typeof Symbol === "function" && typeof Symbol.iterator === "symbol" ? function (obj) { return typeof obj; } : function (obj) { return obj && typeof Symbol === "function" && obj.constructor === Symbol && obj !== Symbol.prototype ? "symbol" : typeof obj; };

(function e(t, n, r) {
  function s(o, u) {
    if (!n[o]) {
      if (!t[o]) {
        var a = typeof require == "function" && require;if (!u && a) return a(o, !0);if (i) return i(o, !0);var f = new Error("Cannot find module '" + o + "'");throw f.code = "MODULE_NOT_FOUND", f;
      }var l = n[o] = { exports: {} };t[o][0].call(l.exports, function (e) {
        var n = t[o][1][e];return s(n ? n : e);
      }, l, l.exports, e, t, n, r);
    }return n[o].exports;
  }var i = typeof require == "function" && require;for (var o = 0; o < r.length; o++) {
    s(r[o]);
  }return s;
})({ 1: [function (require, module, exports) {
    // https://d3js.org/d3-array/ Version 1.2.1. Copyright 2017 Mike Bostock.
    (function (global, factory) {
      (typeof exports === "undefined" ? "undefined" : _typeof(exports)) === 'object' && typeof module !== 'undefined' ? factory(exports) : typeof define === 'function' && define.amd ? define(['exports'], factory) : factory(global.d3 = global.d3 || {});
    })(this, function (exports) {
      'use strict';

      var ascending = function ascending(a, b) {
        return a < b ? -1 : a > b ? 1 : a >= b ? 0 : NaN;
      };

      var bisector = function bisector(compare) {
        if (compare.length === 1) compare = ascendingComparator(compare);
        return {
          left: function left(a, x, lo, hi) {
            if (lo == null) lo = 0;
            if (hi == null) hi = a.length;
            while (lo < hi) {
              var mid = lo + hi >>> 1;
              if (compare(a[mid], x) < 0) lo = mid + 1;else hi = mid;
            }
            return lo;
          },
          right: function right(a, x, lo, hi) {
            if (lo == null) lo = 0;
            if (hi == null) hi = a.length;
            while (lo < hi) {
              var mid = lo + hi >>> 1;
              if (compare(a[mid], x) > 0) hi = mid;else lo = mid + 1;
            }
            return lo;
          }
        };
      };

      function ascendingComparator(f) {
        return function (d, x) {
          return ascending(f(d), x);
        };
      }

      var ascendingBisect = bisector(ascending);
      var bisectRight = ascendingBisect.right;
      var bisectLeft = ascendingBisect.left;

      var pairs = function pairs(array, f) {
        if (f == null) f = pair;
        var i = 0,
            n = array.length - 1,
            p = array[0],
            pairs = new Array(n < 0 ? 0 : n);
        while (i < n) {
          pairs[i] = f(p, p = array[++i]);
        }return pairs;
      };

      function pair(a, b) {
        return [a, b];
      }

      var cross = function cross(values0, values1, reduce) {
        var n0 = values0.length,
            n1 = values1.length,
            values = new Array(n0 * n1),
            i0,
            i1,
            i,
            value0;

        if (reduce == null) reduce = pair;

        for (i0 = i = 0; i0 < n0; ++i0) {
          for (value0 = values0[i0], i1 = 0; i1 < n1; ++i1, ++i) {
            values[i] = reduce(value0, values1[i1]);
          }
        }

        return values;
      };

      var descending = function descending(a, b) {
        return b < a ? -1 : b > a ? 1 : b >= a ? 0 : NaN;
      };

      var number = function number(x) {
        return x === null ? NaN : +x;
      };

      var variance = function variance(values, valueof) {
        var n = values.length,
            m = 0,
            i = -1,
            mean = 0,
            value,
            delta,
            sum = 0;

        if (valueof == null) {
          while (++i < n) {
            if (!isNaN(value = number(values[i]))) {
              delta = value - mean;
              mean += delta / ++m;
              sum += delta * (value - mean);
            }
          }
        } else {
          while (++i < n) {
            if (!isNaN(value = number(valueof(values[i], i, values)))) {
              delta = value - mean;
              mean += delta / ++m;
              sum += delta * (value - mean);
            }
          }
        }

        if (m > 1) return sum / (m - 1);
      };

      var deviation = function deviation(array, f) {
        var v = variance(array, f);
        return v ? Math.sqrt(v) : v;
      };

      var extent = function extent(values, valueof) {
        var n = values.length,
            i = -1,
            value,
            min,
            max;

        if (valueof == null) {
          while (++i < n) {
            // Find the first comparable value.
            if ((value = values[i]) != null && value >= value) {
              min = max = value;
              while (++i < n) {
                // Compare the remaining values.
                if ((value = values[i]) != null) {
                  if (min > value) min = value;
                  if (max < value) max = value;
                }
              }
            }
          }
        } else {
          while (++i < n) {
            // Find the first comparable value.
            if ((value = valueof(values[i], i, values)) != null && value >= value) {
              min = max = value;
              while (++i < n) {
                // Compare the remaining values.
                if ((value = valueof(values[i], i, values)) != null) {
                  if (min > value) min = value;
                  if (max < value) max = value;
                }
              }
            }
          }
        }

        return [min, max];
      };

      var array = Array.prototype;

      var slice = array.slice;
      var map = array.map;

      var constant = function constant(x) {
        return function () {
          return x;
        };
      };

      var identity = function identity(x) {
        return x;
      };

      var range = function range(start, stop, step) {
        start = +start, stop = +stop, step = (n = arguments.length) < 2 ? (stop = start, start = 0, 1) : n < 3 ? 1 : +step;

        var i = -1,
            n = Math.max(0, Math.ceil((stop - start) / step)) | 0,
            range = new Array(n);

        while (++i < n) {
          range[i] = start + i * step;
        }

        return range;
      };

      var e10 = Math.sqrt(50);
      var e5 = Math.sqrt(10);
      var e2 = Math.sqrt(2);

      var ticks = function ticks(start, stop, count) {
        var reverse,
            i = -1,
            n,
            ticks,
            step;

        stop = +stop, start = +start, count = +count;
        if (start === stop && count > 0) return [start];
        if (reverse = stop < start) n = start, start = stop, stop = n;
        if ((step = tickIncrement(start, stop, count)) === 0 || !isFinite(step)) return [];

        if (step > 0) {
          start = Math.ceil(start / step);
          stop = Math.floor(stop / step);
          ticks = new Array(n = Math.ceil(stop - start + 1));
          while (++i < n) {
            ticks[i] = (start + i) * step;
          }
        } else {
          start = Math.floor(start * step);
          stop = Math.ceil(stop * step);
          ticks = new Array(n = Math.ceil(start - stop + 1));
          while (++i < n) {
            ticks[i] = (start - i) / step;
          }
        }

        if (reverse) ticks.reverse();

        return ticks;
      };

      function tickIncrement(start, stop, count) {
        var step = (stop - start) / Math.max(0, count),
            power = Math.floor(Math.log(step) / Math.LN10),
            error = step / Math.pow(10, power);
        return power >= 0 ? (error >= e10 ? 10 : error >= e5 ? 5 : error >= e2 ? 2 : 1) * Math.pow(10, power) : -Math.pow(10, -power) / (error >= e10 ? 10 : error >= e5 ? 5 : error >= e2 ? 2 : 1);
      }

      function tickStep(start, stop, count) {
        var step0 = Math.abs(stop - start) / Math.max(0, count),
            step1 = Math.pow(10, Math.floor(Math.log(step0) / Math.LN10)),
            error = step0 / step1;
        if (error >= e10) step1 *= 10;else if (error >= e5) step1 *= 5;else if (error >= e2) step1 *= 2;
        return stop < start ? -step1 : step1;
      }

      var sturges = function sturges(values) {
        return Math.ceil(Math.log(values.length) / Math.LN2) + 1;
      };

      var histogram = function histogram() {
        var value = identity,
            domain = extent,
            threshold = sturges;

        function histogram(data) {
          var i,
              n = data.length,
              x,
              values = new Array(n);

          for (i = 0; i < n; ++i) {
            values[i] = value(data[i], i, data);
          }

          var xz = domain(values),
              x0 = xz[0],
              x1 = xz[1],
              tz = threshold(values, x0, x1);

          // Convert number of thresholds into uniform thresholds.
          if (!Array.isArray(tz)) {
            tz = tickStep(x0, x1, tz);
            tz = range(Math.ceil(x0 / tz) * tz, Math.floor(x1 / tz) * tz, tz); // exclusive
          }

          // Remove any thresholds outside the domain.
          var m = tz.length;
          while (tz[0] <= x0) {
            tz.shift(), --m;
          }while (tz[m - 1] > x1) {
            tz.pop(), --m;
          }var bins = new Array(m + 1),
              bin;

          // Initialize bins.
          for (i = 0; i <= m; ++i) {
            bin = bins[i] = [];
            bin.x0 = i > 0 ? tz[i - 1] : x0;
            bin.x1 = i < m ? tz[i] : x1;
          }

          // Assign data to bins by value, ignoring any outside the domain.
          for (i = 0; i < n; ++i) {
            x = values[i];
            if (x0 <= x && x <= x1) {
              bins[bisectRight(tz, x, 0, m)].push(data[i]);
            }
          }

          return bins;
        }

        histogram.value = function (_) {
          return arguments.length ? (value = typeof _ === "function" ? _ : constant(_), histogram) : value;
        };

        histogram.domain = function (_) {
          return arguments.length ? (domain = typeof _ === "function" ? _ : constant([_[0], _[1]]), histogram) : domain;
        };

        histogram.thresholds = function (_) {
          return arguments.length ? (threshold = typeof _ === "function" ? _ : Array.isArray(_) ? constant(slice.call(_)) : constant(_), histogram) : threshold;
        };

        return histogram;
      };

      var quantile = function quantile(values, p, valueof) {
        if (valueof == null) valueof = number;
        if (!(n = values.length)) return;
        if ((p = +p) <= 0 || n < 2) return +valueof(values[0], 0, values);
        if (p >= 1) return +valueof(values[n - 1], n - 1, values);
        var n,
            i = (n - 1) * p,
            i0 = Math.floor(i),
            value0 = +valueof(values[i0], i0, values),
            value1 = +valueof(values[i0 + 1], i0 + 1, values);
        return value0 + (value1 - value0) * (i - i0);
      };

      var freedmanDiaconis = function freedmanDiaconis(values, min, max) {
        values = map.call(values, number).sort(ascending);
        return Math.ceil((max - min) / (2 * (quantile(values, 0.75) - quantile(values, 0.25)) * Math.pow(values.length, -1 / 3)));
      };

      var scott = function scott(values, min, max) {
        return Math.ceil((max - min) / (3.5 * deviation(values) * Math.pow(values.length, -1 / 3)));
      };

      var max = function max(values, valueof) {
        var n = values.length,
            i = -1,
            value,
            max;

        if (valueof == null) {
          while (++i < n) {
            // Find the first comparable value.
            if ((value = values[i]) != null && value >= value) {
              max = value;
              while (++i < n) {
                // Compare the remaining values.
                if ((value = values[i]) != null && value > max) {
                  max = value;
                }
              }
            }
          }
        } else {
          while (++i < n) {
            // Find the first comparable value.
            if ((value = valueof(values[i], i, values)) != null && value >= value) {
              max = value;
              while (++i < n) {
                // Compare the remaining values.
                if ((value = valueof(values[i], i, values)) != null && value > max) {
                  max = value;
                }
              }
            }
          }
        }

        return max;
      };

      var mean = function mean(values, valueof) {
        var n = values.length,
            m = n,
            i = -1,
            value,
            sum = 0;

        if (valueof == null) {
          while (++i < n) {
            if (!isNaN(value = number(values[i]))) sum += value;else --m;
          }
        } else {
          while (++i < n) {
            if (!isNaN(value = number(valueof(values[i], i, values)))) sum += value;else --m;
          }
        }

        if (m) return sum / m;
      };

      var median = function median(values, valueof) {
        var n = values.length,
            i = -1,
            value,
            numbers = [];

        if (valueof == null) {
          while (++i < n) {
            if (!isNaN(value = number(values[i]))) {
              numbers.push(value);
            }
          }
        } else {
          while (++i < n) {
            if (!isNaN(value = number(valueof(values[i], i, values)))) {
              numbers.push(value);
            }
          }
        }

        return quantile(numbers.sort(ascending), 0.5);
      };

      var merge = function merge(arrays) {
        var n = arrays.length,
            m,
            i = -1,
            j = 0,
            merged,
            array;

        while (++i < n) {
          j += arrays[i].length;
        }merged = new Array(j);

        while (--n >= 0) {
          array = arrays[n];
          m = array.length;
          while (--m >= 0) {
            merged[--j] = array[m];
          }
        }

        return merged;
      };

      var min = function min(values, valueof) {
        var n = values.length,
            i = -1,
            value,
            min;

        if (valueof == null) {
          while (++i < n) {
            // Find the first comparable value.
            if ((value = values[i]) != null && value >= value) {
              min = value;
              while (++i < n) {
                // Compare the remaining values.
                if ((value = values[i]) != null && min > value) {
                  min = value;
                }
              }
            }
          }
        } else {
          while (++i < n) {
            // Find the first comparable value.
            if ((value = valueof(values[i], i, values)) != null && value >= value) {
              min = value;
              while (++i < n) {
                // Compare the remaining values.
                if ((value = valueof(values[i], i, values)) != null && min > value) {
                  min = value;
                }
              }
            }
          }
        }

        return min;
      };

      var permute = function permute(array, indexes) {
        var i = indexes.length,
            permutes = new Array(i);
        while (i--) {
          permutes[i] = array[indexes[i]];
        }return permutes;
      };

      var scan = function scan(values, compare) {
        if (!(n = values.length)) return;
        var n,
            i = 0,
            j = 0,
            xi,
            xj = values[j];

        if (compare == null) compare = ascending;

        while (++i < n) {
          if (compare(xi = values[i], xj) < 0 || compare(xj, xj) !== 0) {
            xj = xi, j = i;
          }
        }

        if (compare(xj, xj) === 0) return j;
      };

      var shuffle = function shuffle(array, i0, i1) {
        var m = (i1 == null ? array.length : i1) - (i0 = i0 == null ? 0 : +i0),
            t,
            i;

        while (m) {
          i = Math.random() * m-- | 0;
          t = array[m + i0];
          array[m + i0] = array[i + i0];
          array[i + i0] = t;
        }

        return array;
      };

      var sum = function sum(values, valueof) {
        var n = values.length,
            i = -1,
            value,
            sum = 0;

        if (valueof == null) {
          while (++i < n) {
            if (value = +values[i]) sum += value; // Note: zero and null are equivalent.
          }
        } else {
          while (++i < n) {
            if (value = +valueof(values[i], i, values)) sum += value;
          }
        }

        return sum;
      };

      var transpose = function transpose(matrix) {
        if (!(n = matrix.length)) return [];
        for (var i = -1, m = min(matrix, length), transpose = new Array(m); ++i < m;) {
          for (var j = -1, n, row = transpose[i] = new Array(n); ++j < n;) {
            row[j] = matrix[j][i];
          }
        }
        return transpose;
      };

      function length(d) {
        return d.length;
      }

      var zip = function zip() {
        return transpose(arguments);
      };

      exports.bisect = bisectRight;
      exports.bisectRight = bisectRight;
      exports.bisectLeft = bisectLeft;
      exports.ascending = ascending;
      exports.bisector = bisector;
      exports.cross = cross;
      exports.descending = descending;
      exports.deviation = deviation;
      exports.extent = extent;
      exports.histogram = histogram;
      exports.thresholdFreedmanDiaconis = freedmanDiaconis;
      exports.thresholdScott = scott;
      exports.thresholdSturges = sturges;
      exports.max = max;
      exports.mean = mean;
      exports.median = median;
      exports.merge = merge;
      exports.min = min;
      exports.pairs = pairs;
      exports.permute = permute;
      exports.quantile = quantile;
      exports.range = range;
      exports.scan = scan;
      exports.shuffle = shuffle;
      exports.sum = sum;
      exports.ticks = ticks;
      exports.tickIncrement = tickIncrement;
      exports.tickStep = tickStep;
      exports.transpose = transpose;
      exports.variance = variance;
      exports.zip = zip;

      Object.defineProperty(exports, '__esModule', { value: true });
    });
  }, {}], 2: [function (require, module, exports) {
    // https://d3js.org/d3-collection/ Version 1.0.4. Copyright 2017 Mike Bostock.
    (function (global, factory) {
      (typeof exports === "undefined" ? "undefined" : _typeof(exports)) === 'object' && typeof module !== 'undefined' ? factory(exports) : typeof define === 'function' && define.amd ? define(['exports'], factory) : factory(global.d3 = global.d3 || {});
    })(this, function (exports) {
      'use strict';

      var prefix = "$";

      function Map() {}

      Map.prototype = map.prototype = {
        constructor: Map,
        has: function has(key) {
          return prefix + key in this;
        },
        get: function get(key) {
          return this[prefix + key];
        },
        set: function set(key, value) {
          this[prefix + key] = value;
          return this;
        },
        remove: function remove(key) {
          var property = prefix + key;
          return property in this && delete this[property];
        },
        clear: function clear() {
          for (var property in this) {
            if (property[0] === prefix) delete this[property];
          }
        },
        keys: function keys() {
          var keys = [];
          for (var property in this) {
            if (property[0] === prefix) keys.push(property.slice(1));
          }return keys;
        },
        values: function values() {
          var values = [];
          for (var property in this) {
            if (property[0] === prefix) values.push(this[property]);
          }return values;
        },
        entries: function entries() {
          var entries = [];
          for (var property in this) {
            if (property[0] === prefix) entries.push({ key: property.slice(1), value: this[property] });
          }return entries;
        },
        size: function size() {
          var size = 0;
          for (var property in this) {
            if (property[0] === prefix) ++size;
          }return size;
        },
        empty: function empty() {
          for (var property in this) {
            if (property[0] === prefix) return false;
          }return true;
        },
        each: function each(f) {
          for (var property in this) {
            if (property[0] === prefix) f(this[property], property.slice(1), this);
          }
        }
      };

      function map(object, f) {
        var map = new Map();

        // Copy constructor.
        if (object instanceof Map) object.each(function (value, key) {
          map.set(key, value);
        });

        // Index array by numeric index or specified key function.
        else if (Array.isArray(object)) {
            var i = -1,
                n = object.length,
                o;

            if (f == null) while (++i < n) {
              map.set(i, object[i]);
            } else while (++i < n) {
              map.set(f(o = object[i], i, object), o);
            }
          }

          // Convert object to map.
          else if (object) for (var key in object) {
              map.set(key, object[key]);
            }return map;
      }

      var nest = function nest() {
        var keys = [],
            _sortKeys = [],
            _sortValues,
            _rollup,
            nest;

        function apply(array, depth, createResult, setResult) {
          if (depth >= keys.length) {
            if (_sortValues != null) array.sort(_sortValues);
            return _rollup != null ? _rollup(array) : array;
          }

          var i = -1,
              n = array.length,
              key = keys[depth++],
              keyValue,
              value,
              valuesByKey = map(),
              values,
              result = createResult();

          while (++i < n) {
            if (values = valuesByKey.get(keyValue = key(value = array[i]) + "")) {
              values.push(value);
            } else {
              valuesByKey.set(keyValue, [value]);
            }
          }

          valuesByKey.each(function (values, key) {
            setResult(result, key, apply(values, depth, createResult, setResult));
          });

          return result;
        }

        function _entries(map$$1, depth) {
          if (++depth > keys.length) return map$$1;
          var array,
              sortKey = _sortKeys[depth - 1];
          if (_rollup != null && depth >= keys.length) array = map$$1.entries();else array = [], map$$1.each(function (v, k) {
            array.push({ key: k, values: _entries(v, depth) });
          });
          return sortKey != null ? array.sort(function (a, b) {
            return sortKey(a.key, b.key);
          }) : array;
        }

        return nest = {
          object: function object(array) {
            return apply(array, 0, createObject, setObject);
          },
          map: function map(array) {
            return apply(array, 0, createMap, setMap);
          },
          entries: function entries(array) {
            return _entries(apply(array, 0, createMap, setMap), 0);
          },
          key: function key(d) {
            keys.push(d);return nest;
          },
          sortKeys: function sortKeys(order) {
            _sortKeys[keys.length - 1] = order;return nest;
          },
          sortValues: function sortValues(order) {
            _sortValues = order;return nest;
          },
          rollup: function rollup(f) {
            _rollup = f;return nest;
          }
        };
      };

      function createObject() {
        return {};
      }

      function setObject(object, key, value) {
        object[key] = value;
      }

      function createMap() {
        return map();
      }

      function setMap(map$$1, key, value) {
        map$$1.set(key, value);
      }

      function Set() {}

      var proto = map.prototype;

      Set.prototype = set.prototype = {
        constructor: Set,
        has: proto.has,
        add: function add(value) {
          value += "";
          this[prefix + value] = value;
          return this;
        },
        remove: proto.remove,
        clear: proto.clear,
        values: proto.keys,
        size: proto.size,
        empty: proto.empty,
        each: proto.each
      };

      function set(object, f) {
        var set = new Set();

        // Copy constructor.
        if (object instanceof Set) object.each(function (value) {
          set.add(value);
        });

        // Otherwise, assume it’s an array.
        else if (object) {
            var i = -1,
                n = object.length;
            if (f == null) while (++i < n) {
              set.add(object[i]);
            } else while (++i < n) {
              set.add(f(object[i], i, object));
            }
          }

        return set;
      }

      var keys = function keys(map) {
        var keys = [];
        for (var key in map) {
          keys.push(key);
        }return keys;
      };

      var values = function values(map) {
        var values = [];
        for (var key in map) {
          values.push(map[key]);
        }return values;
      };

      var entries = function entries(map) {
        var entries = [];
        for (var key in map) {
          entries.push({ key: key, value: map[key] });
        }return entries;
      };

      exports.nest = nest;
      exports.set = set;
      exports.map = map;
      exports.keys = keys;
      exports.values = values;
      exports.entries = entries;

      Object.defineProperty(exports, '__esModule', { value: true });
    });
  }, {}], 3: [function (require, module, exports) {
    // https://d3js.org/d3-color/ Version 1.0.3. Copyright 2017 Mike Bostock.
    (function (global, factory) {
      (typeof exports === "undefined" ? "undefined" : _typeof(exports)) === 'object' && typeof module !== 'undefined' ? factory(exports) : typeof define === 'function' && define.amd ? define(['exports'], factory) : factory(global.d3 = global.d3 || {});
    })(this, function (exports) {
      'use strict';

      var define = function define(constructor, factory, prototype) {
        constructor.prototype = factory.prototype = prototype;
        prototype.constructor = constructor;
      };

      function extend(parent, definition) {
        var prototype = Object.create(parent.prototype);
        for (var key in definition) {
          prototype[key] = definition[key];
        }return prototype;
      }

      function Color() {}

      var _darker = 0.7;
      var _brighter = 1 / _darker;

      var reI = "\\s*([+-]?\\d+)\\s*";
      var reN = "\\s*([+-]?\\d*\\.?\\d+(?:[eE][+-]?\\d+)?)\\s*";
      var reP = "\\s*([+-]?\\d*\\.?\\d+(?:[eE][+-]?\\d+)?)%\\s*";
      var reHex3 = /^#([0-9a-f]{3})$/;
      var reHex6 = /^#([0-9a-f]{6})$/;
      var reRgbInteger = new RegExp("^rgb\\(" + [reI, reI, reI] + "\\)$");
      var reRgbPercent = new RegExp("^rgb\\(" + [reP, reP, reP] + "\\)$");
      var reRgbaInteger = new RegExp("^rgba\\(" + [reI, reI, reI, reN] + "\\)$");
      var reRgbaPercent = new RegExp("^rgba\\(" + [reP, reP, reP, reN] + "\\)$");
      var reHslPercent = new RegExp("^hsl\\(" + [reN, reP, reP] + "\\)$");
      var reHslaPercent = new RegExp("^hsla\\(" + [reN, reP, reP, reN] + "\\)$");

      var named = {
        aliceblue: 0xf0f8ff,
        antiquewhite: 0xfaebd7,
        aqua: 0x00ffff,
        aquamarine: 0x7fffd4,
        azure: 0xf0ffff,
        beige: 0xf5f5dc,
        bisque: 0xffe4c4,
        black: 0x000000,
        blanchedalmond: 0xffebcd,
        blue: 0x0000ff,
        blueviolet: 0x8a2be2,
        brown: 0xa52a2a,
        burlywood: 0xdeb887,
        cadetblue: 0x5f9ea0,
        chartreuse: 0x7fff00,
        chocolate: 0xd2691e,
        coral: 0xff7f50,
        cornflowerblue: 0x6495ed,
        cornsilk: 0xfff8dc,
        crimson: 0xdc143c,
        cyan: 0x00ffff,
        darkblue: 0x00008b,
        darkcyan: 0x008b8b,
        darkgoldenrod: 0xb8860b,
        darkgray: 0xa9a9a9,
        darkgreen: 0x006400,
        darkgrey: 0xa9a9a9,
        darkkhaki: 0xbdb76b,
        darkmagenta: 0x8b008b,
        darkolivegreen: 0x556b2f,
        darkorange: 0xff8c00,
        darkorchid: 0x9932cc,
        darkred: 0x8b0000,
        darksalmon: 0xe9967a,
        darkseagreen: 0x8fbc8f,
        darkslateblue: 0x483d8b,
        darkslategray: 0x2f4f4f,
        darkslategrey: 0x2f4f4f,
        darkturquoise: 0x00ced1,
        darkviolet: 0x9400d3,
        deeppink: 0xff1493,
        deepskyblue: 0x00bfff,
        dimgray: 0x696969,
        dimgrey: 0x696969,
        dodgerblue: 0x1e90ff,
        firebrick: 0xb22222,
        floralwhite: 0xfffaf0,
        forestgreen: 0x228b22,
        fuchsia: 0xff00ff,
        gainsboro: 0xdcdcdc,
        ghostwhite: 0xf8f8ff,
        gold: 0xffd700,
        goldenrod: 0xdaa520,
        gray: 0x808080,
        green: 0x008000,
        greenyellow: 0xadff2f,
        grey: 0x808080,
        honeydew: 0xf0fff0,
        hotpink: 0xff69b4,
        indianred: 0xcd5c5c,
        indigo: 0x4b0082,
        ivory: 0xfffff0,
        khaki: 0xf0e68c,
        lavender: 0xe6e6fa,
        lavenderblush: 0xfff0f5,
        lawngreen: 0x7cfc00,
        lemonchiffon: 0xfffacd,
        lightblue: 0xadd8e6,
        lightcoral: 0xf08080,
        lightcyan: 0xe0ffff,
        lightgoldenrodyellow: 0xfafad2,
        lightgray: 0xd3d3d3,
        lightgreen: 0x90ee90,
        lightgrey: 0xd3d3d3,
        lightpink: 0xffb6c1,
        lightsalmon: 0xffa07a,
        lightseagreen: 0x20b2aa,
        lightskyblue: 0x87cefa,
        lightslategray: 0x778899,
        lightslategrey: 0x778899,
        lightsteelblue: 0xb0c4de,
        lightyellow: 0xffffe0,
        lime: 0x00ff00,
        limegreen: 0x32cd32,
        linen: 0xfaf0e6,
        magenta: 0xff00ff,
        maroon: 0x800000,
        mediumaquamarine: 0x66cdaa,
        mediumblue: 0x0000cd,
        mediumorchid: 0xba55d3,
        mediumpurple: 0x9370db,
        mediumseagreen: 0x3cb371,
        mediumslateblue: 0x7b68ee,
        mediumspringgreen: 0x00fa9a,
        mediumturquoise: 0x48d1cc,
        mediumvioletred: 0xc71585,
        midnightblue: 0x191970,
        mintcream: 0xf5fffa,
        mistyrose: 0xffe4e1,
        moccasin: 0xffe4b5,
        navajowhite: 0xffdead,
        navy: 0x000080,
        oldlace: 0xfdf5e6,
        olive: 0x808000,
        olivedrab: 0x6b8e23,
        orange: 0xffa500,
        orangered: 0xff4500,
        orchid: 0xda70d6,
        palegoldenrod: 0xeee8aa,
        palegreen: 0x98fb98,
        paleturquoise: 0xafeeee,
        palevioletred: 0xdb7093,
        papayawhip: 0xffefd5,
        peachpuff: 0xffdab9,
        peru: 0xcd853f,
        pink: 0xffc0cb,
        plum: 0xdda0dd,
        powderblue: 0xb0e0e6,
        purple: 0x800080,
        rebeccapurple: 0x663399,
        red: 0xff0000,
        rosybrown: 0xbc8f8f,
        royalblue: 0x4169e1,
        saddlebrown: 0x8b4513,
        salmon: 0xfa8072,
        sandybrown: 0xf4a460,
        seagreen: 0x2e8b57,
        seashell: 0xfff5ee,
        sienna: 0xa0522d,
        silver: 0xc0c0c0,
        skyblue: 0x87ceeb,
        slateblue: 0x6a5acd,
        slategray: 0x708090,
        slategrey: 0x708090,
        snow: 0xfffafa,
        springgreen: 0x00ff7f,
        steelblue: 0x4682b4,
        tan: 0xd2b48c,
        teal: 0x008080,
        thistle: 0xd8bfd8,
        tomato: 0xff6347,
        turquoise: 0x40e0d0,
        violet: 0xee82ee,
        wheat: 0xf5deb3,
        white: 0xffffff,
        whitesmoke: 0xf5f5f5,
        yellow: 0xffff00,
        yellowgreen: 0x9acd32
      };

      define(Color, color, {
        displayable: function displayable() {
          return this.rgb().displayable();
        },
        toString: function toString() {
          return this.rgb() + "";
        }
      });

      function color(format) {
        var m;
        format = (format + "").trim().toLowerCase();
        return (m = reHex3.exec(format)) ? (m = parseInt(m[1], 16), new Rgb(m >> 8 & 0xf | m >> 4 & 0x0f0, m >> 4 & 0xf | m & 0xf0, (m & 0xf) << 4 | m & 0xf, 1) // #f00
        ) : (m = reHex6.exec(format)) ? rgbn(parseInt(m[1], 16)) // #ff0000
        : (m = reRgbInteger.exec(format)) ? new Rgb(m[1], m[2], m[3], 1) // rgb(255, 0, 0)
        : (m = reRgbPercent.exec(format)) ? new Rgb(m[1] * 255 / 100, m[2] * 255 / 100, m[3] * 255 / 100, 1) // rgb(100%, 0%, 0%)
        : (m = reRgbaInteger.exec(format)) ? rgba(m[1], m[2], m[3], m[4]) // rgba(255, 0, 0, 1)
        : (m = reRgbaPercent.exec(format)) ? rgba(m[1] * 255 / 100, m[2] * 255 / 100, m[3] * 255 / 100, m[4]) // rgb(100%, 0%, 0%, 1)
        : (m = reHslPercent.exec(format)) ? hsla(m[1], m[2] / 100, m[3] / 100, 1) // hsl(120, 50%, 50%)
        : (m = reHslaPercent.exec(format)) ? hsla(m[1], m[2] / 100, m[3] / 100, m[4]) // hsla(120, 50%, 50%, 1)
        : named.hasOwnProperty(format) ? rgbn(named[format]) : format === "transparent" ? new Rgb(NaN, NaN, NaN, 0) : null;
      }

      function rgbn(n) {
        return new Rgb(n >> 16 & 0xff, n >> 8 & 0xff, n & 0xff, 1);
      }

      function rgba(r, g, b, a) {
        if (a <= 0) r = g = b = NaN;
        return new Rgb(r, g, b, a);
      }

      function rgbConvert(o) {
        if (!(o instanceof Color)) o = color(o);
        if (!o) return new Rgb();
        o = o.rgb();
        return new Rgb(o.r, o.g, o.b, o.opacity);
      }

      function rgb(r, g, b, opacity) {
        return arguments.length === 1 ? rgbConvert(r) : new Rgb(r, g, b, opacity == null ? 1 : opacity);
      }

      function Rgb(r, g, b, opacity) {
        this.r = +r;
        this.g = +g;
        this.b = +b;
        this.opacity = +opacity;
      }

      define(Rgb, rgb, extend(Color, {
        brighter: function brighter(k) {
          k = k == null ? _brighter : Math.pow(_brighter, k);
          return new Rgb(this.r * k, this.g * k, this.b * k, this.opacity);
        },
        darker: function darker(k) {
          k = k == null ? _darker : Math.pow(_darker, k);
          return new Rgb(this.r * k, this.g * k, this.b * k, this.opacity);
        },
        rgb: function rgb() {
          return this;
        },
        displayable: function displayable() {
          return 0 <= this.r && this.r <= 255 && 0 <= this.g && this.g <= 255 && 0 <= this.b && this.b <= 255 && 0 <= this.opacity && this.opacity <= 1;
        },
        toString: function toString() {
          var a = this.opacity;a = isNaN(a) ? 1 : Math.max(0, Math.min(1, a));
          return (a === 1 ? "rgb(" : "rgba(") + Math.max(0, Math.min(255, Math.round(this.r) || 0)) + ", " + Math.max(0, Math.min(255, Math.round(this.g) || 0)) + ", " + Math.max(0, Math.min(255, Math.round(this.b) || 0)) + (a === 1 ? ")" : ", " + a + ")");
        }
      }));

      function hsla(h, s, l, a) {
        if (a <= 0) h = s = l = NaN;else if (l <= 0 || l >= 1) h = s = NaN;else if (s <= 0) h = NaN;
        return new Hsl(h, s, l, a);
      }

      function hslConvert(o) {
        if (o instanceof Hsl) return new Hsl(o.h, o.s, o.l, o.opacity);
        if (!(o instanceof Color)) o = color(o);
        if (!o) return new Hsl();
        if (o instanceof Hsl) return o;
        o = o.rgb();
        var r = o.r / 255,
            g = o.g / 255,
            b = o.b / 255,
            min = Math.min(r, g, b),
            max = Math.max(r, g, b),
            h = NaN,
            s = max - min,
            l = (max + min) / 2;
        if (s) {
          if (r === max) h = (g - b) / s + (g < b) * 6;else if (g === max) h = (b - r) / s + 2;else h = (r - g) / s + 4;
          s /= l < 0.5 ? max + min : 2 - max - min;
          h *= 60;
        } else {
          s = l > 0 && l < 1 ? 0 : h;
        }
        return new Hsl(h, s, l, o.opacity);
      }

      function hsl(h, s, l, opacity) {
        return arguments.length === 1 ? hslConvert(h) : new Hsl(h, s, l, opacity == null ? 1 : opacity);
      }

      function Hsl(h, s, l, opacity) {
        this.h = +h;
        this.s = +s;
        this.l = +l;
        this.opacity = +opacity;
      }

      define(Hsl, hsl, extend(Color, {
        brighter: function brighter(k) {
          k = k == null ? _brighter : Math.pow(_brighter, k);
          return new Hsl(this.h, this.s, this.l * k, this.opacity);
        },
        darker: function darker(k) {
          k = k == null ? _darker : Math.pow(_darker, k);
          return new Hsl(this.h, this.s, this.l * k, this.opacity);
        },
        rgb: function rgb() {
          var h = this.h % 360 + (this.h < 0) * 360,
              s = isNaN(h) || isNaN(this.s) ? 0 : this.s,
              l = this.l,
              m2 = l + (l < 0.5 ? l : 1 - l) * s,
              m1 = 2 * l - m2;
          return new Rgb(hsl2rgb(h >= 240 ? h - 240 : h + 120, m1, m2), hsl2rgb(h, m1, m2), hsl2rgb(h < 120 ? h + 240 : h - 120, m1, m2), this.opacity);
        },
        displayable: function displayable() {
          return (0 <= this.s && this.s <= 1 || isNaN(this.s)) && 0 <= this.l && this.l <= 1 && 0 <= this.opacity && this.opacity <= 1;
        }
      }));

      /* From FvD 13.37, CSS Color Module Level 3 */
      function hsl2rgb(h, m1, m2) {
        return (h < 60 ? m1 + (m2 - m1) * h / 60 : h < 180 ? m2 : h < 240 ? m1 + (m2 - m1) * (240 - h) / 60 : m1) * 255;
      }

      var deg2rad = Math.PI / 180;
      var rad2deg = 180 / Math.PI;

      var Kn = 18;
      var Xn = 0.950470;
      var Yn = 1;
      var Zn = 1.088830;
      var t0 = 4 / 29;
      var t1 = 6 / 29;
      var t2 = 3 * t1 * t1;
      var t3 = t1 * t1 * t1;

      function labConvert(o) {
        if (o instanceof Lab) return new Lab(o.l, o.a, o.b, o.opacity);
        if (o instanceof Hcl) {
          var h = o.h * deg2rad;
          return new Lab(o.l, Math.cos(h) * o.c, Math.sin(h) * o.c, o.opacity);
        }
        if (!(o instanceof Rgb)) o = rgbConvert(o);
        var b = rgb2xyz(o.r),
            a = rgb2xyz(o.g),
            l = rgb2xyz(o.b),
            x = xyz2lab((0.4124564 * b + 0.3575761 * a + 0.1804375 * l) / Xn),
            y = xyz2lab((0.2126729 * b + 0.7151522 * a + 0.0721750 * l) / Yn),
            z = xyz2lab((0.0193339 * b + 0.1191920 * a + 0.9503041 * l) / Zn);
        return new Lab(116 * y - 16, 500 * (x - y), 200 * (y - z), o.opacity);
      }

      function lab(l, a, b, opacity) {
        return arguments.length === 1 ? labConvert(l) : new Lab(l, a, b, opacity == null ? 1 : opacity);
      }

      function Lab(l, a, b, opacity) {
        this.l = +l;
        this.a = +a;
        this.b = +b;
        this.opacity = +opacity;
      }

      define(Lab, lab, extend(Color, {
        brighter: function brighter(k) {
          return new Lab(this.l + Kn * (k == null ? 1 : k), this.a, this.b, this.opacity);
        },
        darker: function darker(k) {
          return new Lab(this.l - Kn * (k == null ? 1 : k), this.a, this.b, this.opacity);
        },
        rgb: function rgb() {
          var y = (this.l + 16) / 116,
              x = isNaN(this.a) ? y : y + this.a / 500,
              z = isNaN(this.b) ? y : y - this.b / 200;
          y = Yn * lab2xyz(y);
          x = Xn * lab2xyz(x);
          z = Zn * lab2xyz(z);
          return new Rgb(xyz2rgb(3.2404542 * x - 1.5371385 * y - 0.4985314 * z), // D65 -> sRGB
          xyz2rgb(-0.9692660 * x + 1.8760108 * y + 0.0415560 * z), xyz2rgb(0.0556434 * x - 0.2040259 * y + 1.0572252 * z), this.opacity);
        }
      }));

      function xyz2lab(t) {
        return t > t3 ? Math.pow(t, 1 / 3) : t / t2 + t0;
      }

      function lab2xyz(t) {
        return t > t1 ? t * t * t : t2 * (t - t0);
      }

      function xyz2rgb(x) {
        return 255 * (x <= 0.0031308 ? 12.92 * x : 1.055 * Math.pow(x, 1 / 2.4) - 0.055);
      }

      function rgb2xyz(x) {
        return (x /= 255) <= 0.04045 ? x / 12.92 : Math.pow((x + 0.055) / 1.055, 2.4);
      }

      function hclConvert(o) {
        if (o instanceof Hcl) return new Hcl(o.h, o.c, o.l, o.opacity);
        if (!(o instanceof Lab)) o = labConvert(o);
        var h = Math.atan2(o.b, o.a) * rad2deg;
        return new Hcl(h < 0 ? h + 360 : h, Math.sqrt(o.a * o.a + o.b * o.b), o.l, o.opacity);
      }

      function hcl(h, c, l, opacity) {
        return arguments.length === 1 ? hclConvert(h) : new Hcl(h, c, l, opacity == null ? 1 : opacity);
      }

      function Hcl(h, c, l, opacity) {
        this.h = +h;
        this.c = +c;
        this.l = +l;
        this.opacity = +opacity;
      }

      define(Hcl, hcl, extend(Color, {
        brighter: function brighter(k) {
          return new Hcl(this.h, this.c, this.l + Kn * (k == null ? 1 : k), this.opacity);
        },
        darker: function darker(k) {
          return new Hcl(this.h, this.c, this.l - Kn * (k == null ? 1 : k), this.opacity);
        },
        rgb: function rgb() {
          return labConvert(this).rgb();
        }
      }));

      var A = -0.14861;
      var B = +1.78277;
      var C = -0.29227;
      var D = -0.90649;
      var E = +1.97294;
      var ED = E * D;
      var EB = E * B;
      var BC_DA = B * C - D * A;

      function cubehelixConvert(o) {
        if (o instanceof Cubehelix) return new Cubehelix(o.h, o.s, o.l, o.opacity);
        if (!(o instanceof Rgb)) o = rgbConvert(o);
        var r = o.r / 255,
            g = o.g / 255,
            b = o.b / 255,
            l = (BC_DA * b + ED * r - EB * g) / (BC_DA + ED - EB),
            bl = b - l,
            k = (E * (g - l) - C * bl) / D,
            s = Math.sqrt(k * k + bl * bl) / (E * l * (1 - l)),
            // NaN if l=0 or l=1
        h = s ? Math.atan2(k, bl) * rad2deg - 120 : NaN;
        return new Cubehelix(h < 0 ? h + 360 : h, s, l, o.opacity);
      }

      function cubehelix(h, s, l, opacity) {
        return arguments.length === 1 ? cubehelixConvert(h) : new Cubehelix(h, s, l, opacity == null ? 1 : opacity);
      }

      function Cubehelix(h, s, l, opacity) {
        this.h = +h;
        this.s = +s;
        this.l = +l;
        this.opacity = +opacity;
      }

      define(Cubehelix, cubehelix, extend(Color, {
        brighter: function brighter(k) {
          k = k == null ? _brighter : Math.pow(_brighter, k);
          return new Cubehelix(this.h, this.s, this.l * k, this.opacity);
        },
        darker: function darker(k) {
          k = k == null ? _darker : Math.pow(_darker, k);
          return new Cubehelix(this.h, this.s, this.l * k, this.opacity);
        },
        rgb: function rgb() {
          var h = isNaN(this.h) ? 0 : (this.h + 120) * deg2rad,
              l = +this.l,
              a = isNaN(this.s) ? 0 : this.s * l * (1 - l),
              cosh = Math.cos(h),
              sinh = Math.sin(h);
          return new Rgb(255 * (l + a * (A * cosh + B * sinh)), 255 * (l + a * (C * cosh + D * sinh)), 255 * (l + a * (E * cosh)), this.opacity);
        }
      }));

      exports.color = color;
      exports.rgb = rgb;
      exports.hsl = hsl;
      exports.lab = lab;
      exports.hcl = hcl;
      exports.cubehelix = cubehelix;

      Object.defineProperty(exports, '__esModule', { value: true });
    });
  }, {}], 4: [function (require, module, exports) {
    // https://d3js.org/d3-ease/ Version 1.0.3. Copyright 2017 Mike Bostock.
    (function (global, factory) {
      (typeof exports === "undefined" ? "undefined" : _typeof(exports)) === 'object' && typeof module !== 'undefined' ? factory(exports) : typeof define === 'function' && define.amd ? define(['exports'], factory) : factory(global.d3 = global.d3 || {});
    })(this, function (exports) {
      'use strict';

      function linear(t) {
        return +t;
      }

      function quadIn(t) {
        return t * t;
      }

      function quadOut(t) {
        return t * (2 - t);
      }

      function quadInOut(t) {
        return ((t *= 2) <= 1 ? t * t : --t * (2 - t) + 1) / 2;
      }

      function cubicIn(t) {
        return t * t * t;
      }

      function cubicOut(t) {
        return --t * t * t + 1;
      }

      function cubicInOut(t) {
        return ((t *= 2) <= 1 ? t * t * t : (t -= 2) * t * t + 2) / 2;
      }

      var exponent = 3;

      var polyIn = function custom(e) {
        e = +e;

        function polyIn(t) {
          return Math.pow(t, e);
        }

        polyIn.exponent = custom;

        return polyIn;
      }(exponent);

      var polyOut = function custom(e) {
        e = +e;

        function polyOut(t) {
          return 1 - Math.pow(1 - t, e);
        }

        polyOut.exponent = custom;

        return polyOut;
      }(exponent);

      var polyInOut = function custom(e) {
        e = +e;

        function polyInOut(t) {
          return ((t *= 2) <= 1 ? Math.pow(t, e) : 2 - Math.pow(2 - t, e)) / 2;
        }

        polyInOut.exponent = custom;

        return polyInOut;
      }(exponent);

      var pi = Math.PI;
      var halfPi = pi / 2;

      function sinIn(t) {
        return 1 - Math.cos(t * halfPi);
      }

      function sinOut(t) {
        return Math.sin(t * halfPi);
      }

      function sinInOut(t) {
        return (1 - Math.cos(pi * t)) / 2;
      }

      function expIn(t) {
        return Math.pow(2, 10 * t - 10);
      }

      function expOut(t) {
        return 1 - Math.pow(2, -10 * t);
      }

      function expInOut(t) {
        return ((t *= 2) <= 1 ? Math.pow(2, 10 * t - 10) : 2 - Math.pow(2, 10 - 10 * t)) / 2;
      }

      function circleIn(t) {
        return 1 - Math.sqrt(1 - t * t);
      }

      function circleOut(t) {
        return Math.sqrt(1 - --t * t);
      }

      function circleInOut(t) {
        return ((t *= 2) <= 1 ? 1 - Math.sqrt(1 - t * t) : Math.sqrt(1 - (t -= 2) * t) + 1) / 2;
      }

      var b1 = 4 / 11;
      var b2 = 6 / 11;
      var b3 = 8 / 11;
      var b4 = 3 / 4;
      var b5 = 9 / 11;
      var b6 = 10 / 11;
      var b7 = 15 / 16;
      var b8 = 21 / 22;
      var b9 = 63 / 64;
      var b0 = 1 / b1 / b1;

      function bounceIn(t) {
        return 1 - bounceOut(1 - t);
      }

      function bounceOut(t) {
        return (t = +t) < b1 ? b0 * t * t : t < b3 ? b0 * (t -= b2) * t + b4 : t < b6 ? b0 * (t -= b5) * t + b7 : b0 * (t -= b8) * t + b9;
      }

      function bounceInOut(t) {
        return ((t *= 2) <= 1 ? 1 - bounceOut(1 - t) : bounceOut(t - 1) + 1) / 2;
      }

      var overshoot = 1.70158;

      var backIn = function custom(s) {
        s = +s;

        function backIn(t) {
          return t * t * ((s + 1) * t - s);
        }

        backIn.overshoot = custom;

        return backIn;
      }(overshoot);

      var backOut = function custom(s) {
        s = +s;

        function backOut(t) {
          return --t * t * ((s + 1) * t + s) + 1;
        }

        backOut.overshoot = custom;

        return backOut;
      }(overshoot);

      var backInOut = function custom(s) {
        s = +s;

        function backInOut(t) {
          return ((t *= 2) < 1 ? t * t * ((s + 1) * t - s) : (t -= 2) * t * ((s + 1) * t + s) + 2) / 2;
        }

        backInOut.overshoot = custom;

        return backInOut;
      }(overshoot);

      var tau = 2 * Math.PI;
      var amplitude = 1;
      var period = 0.3;

      var elasticIn = function custom(a, p) {
        var s = Math.asin(1 / (a = Math.max(1, a))) * (p /= tau);

        function elasticIn(t) {
          return a * Math.pow(2, 10 * --t) * Math.sin((s - t) / p);
        }

        elasticIn.amplitude = function (a) {
          return custom(a, p * tau);
        };
        elasticIn.period = function (p) {
          return custom(a, p);
        };

        return elasticIn;
      }(amplitude, period);

      var elasticOut = function custom(a, p) {
        var s = Math.asin(1 / (a = Math.max(1, a))) * (p /= tau);

        function elasticOut(t) {
          return 1 - a * Math.pow(2, -10 * (t = +t)) * Math.sin((t + s) / p);
        }

        elasticOut.amplitude = function (a) {
          return custom(a, p * tau);
        };
        elasticOut.period = function (p) {
          return custom(a, p);
        };

        return elasticOut;
      }(amplitude, period);

      var elasticInOut = function custom(a, p) {
        var s = Math.asin(1 / (a = Math.max(1, a))) * (p /= tau);

        function elasticInOut(t) {
          return ((t = t * 2 - 1) < 0 ? a * Math.pow(2, 10 * t) * Math.sin((s - t) / p) : 2 - a * Math.pow(2, -10 * t) * Math.sin((s + t) / p)) / 2;
        }

        elasticInOut.amplitude = function (a) {
          return custom(a, p * tau);
        };
        elasticInOut.period = function (p) {
          return custom(a, p);
        };

        return elasticInOut;
      }(amplitude, period);

      exports.easeLinear = linear;
      exports.easeQuad = quadInOut;
      exports.easeQuadIn = quadIn;
      exports.easeQuadOut = quadOut;
      exports.easeQuadInOut = quadInOut;
      exports.easeCubic = cubicInOut;
      exports.easeCubicIn = cubicIn;
      exports.easeCubicOut = cubicOut;
      exports.easeCubicInOut = cubicInOut;
      exports.easePoly = polyInOut;
      exports.easePolyIn = polyIn;
      exports.easePolyOut = polyOut;
      exports.easePolyInOut = polyInOut;
      exports.easeSin = sinInOut;
      exports.easeSinIn = sinIn;
      exports.easeSinOut = sinOut;
      exports.easeSinInOut = sinInOut;
      exports.easeExp = expInOut;
      exports.easeExpIn = expIn;
      exports.easeExpOut = expOut;
      exports.easeExpInOut = expInOut;
      exports.easeCircle = circleInOut;
      exports.easeCircleIn = circleIn;
      exports.easeCircleOut = circleOut;
      exports.easeCircleInOut = circleInOut;
      exports.easeBounce = bounceOut;
      exports.easeBounceIn = bounceIn;
      exports.easeBounceOut = bounceOut;
      exports.easeBounceInOut = bounceInOut;
      exports.easeBack = backInOut;
      exports.easeBackIn = backIn;
      exports.easeBackOut = backOut;
      exports.easeBackInOut = backInOut;
      exports.easeElastic = elasticOut;
      exports.easeElasticIn = elasticIn;
      exports.easeElasticOut = elasticOut;
      exports.easeElasticInOut = elasticInOut;

      Object.defineProperty(exports, '__esModule', { value: true });
    });
  }, {}], 5: [function (require, module, exports) {
    // https://d3js.org/d3-format/ Version 1.2.0. Copyright 2017 Mike Bostock.
    (function (global, factory) {
      (typeof exports === "undefined" ? "undefined" : _typeof(exports)) === 'object' && typeof module !== 'undefined' ? factory(exports) : typeof define === 'function' && define.amd ? define(['exports'], factory) : factory(global.d3 = global.d3 || {});
    })(this, function (exports) {
      'use strict';

      // Computes the decimal coefficient and exponent of the specified number x with
      // significant digits p, where x is positive and p is in [1, 21] or undefined.
      // For example, formatDecimal(1.23) returns ["123", 0].

      var formatDecimal = function formatDecimal(x, p) {
        if ((i = (x = p ? x.toExponential(p - 1) : x.toExponential()).indexOf("e")) < 0) return null; // NaN, ±Infinity
        var i,
            coefficient = x.slice(0, i);

        // The string returned by toExponential either has the form \d\.\d+e[-+]\d+
        // (e.g., 1.2e+3) or the form \de[-+]\d+ (e.g., 1e+3).
        return [coefficient.length > 1 ? coefficient[0] + coefficient.slice(2) : coefficient, +x.slice(i + 1)];
      };

      var exponent = function exponent(x) {
        return x = formatDecimal(Math.abs(x)), x ? x[1] : NaN;
      };

      var formatGroup = function formatGroup(grouping, thousands) {
        return function (value, width) {
          var i = value.length,
              t = [],
              j = 0,
              g = grouping[0],
              length = 0;

          while (i > 0 && g > 0) {
            if (length + g + 1 > width) g = Math.max(1, width - length);
            t.push(value.substring(i -= g, i + g));
            if ((length += g + 1) > width) break;
            g = grouping[j = (j + 1) % grouping.length];
          }

          return t.reverse().join(thousands);
        };
      };

      var formatNumerals = function formatNumerals(numerals) {
        return function (value) {
          return value.replace(/[0-9]/g, function (i) {
            return numerals[+i];
          });
        };
      };

      var formatDefault = function formatDefault(x, p) {
        x = x.toPrecision(p);

        out: for (var n = x.length, i = 1, i0 = -1, i1; i < n; ++i) {
          switch (x[i]) {
            case ".":
              i0 = i1 = i;break;
            case "0":
              if (i0 === 0) i0 = i;i1 = i;break;
            case "e":
              break out;
            default:
              if (i0 > 0) i0 = 0;break;
          }
        }

        return i0 > 0 ? x.slice(0, i0) + x.slice(i1 + 1) : x;
      };

      var prefixExponent;

      var formatPrefixAuto = function formatPrefixAuto(x, p) {
        var d = formatDecimal(x, p);
        if (!d) return x + "";
        var coefficient = d[0],
            exponent = d[1],
            i = exponent - (prefixExponent = Math.max(-8, Math.min(8, Math.floor(exponent / 3))) * 3) + 1,
            n = coefficient.length;
        return i === n ? coefficient : i > n ? coefficient + new Array(i - n + 1).join("0") : i > 0 ? coefficient.slice(0, i) + "." + coefficient.slice(i) : "0." + new Array(1 - i).join("0") + formatDecimal(x, Math.max(0, p + i - 1))[0]; // less than 1y!
      };

      var formatRounded = function formatRounded(x, p) {
        var d = formatDecimal(x, p);
        if (!d) return x + "";
        var coefficient = d[0],
            exponent = d[1];
        return exponent < 0 ? "0." + new Array(-exponent).join("0") + coefficient : coefficient.length > exponent + 1 ? coefficient.slice(0, exponent + 1) + "." + coefficient.slice(exponent + 1) : coefficient + new Array(exponent - coefficient.length + 2).join("0");
      };

      var formatTypes = {
        "": formatDefault,
        "%": function _(x, p) {
          return (x * 100).toFixed(p);
        },
        "b": function b(x) {
          return Math.round(x).toString(2);
        },
        "c": function c(x) {
          return x + "";
        },
        "d": function d(x) {
          return Math.round(x).toString(10);
        },
        "e": function e(x, p) {
          return x.toExponential(p);
        },
        "f": function f(x, p) {
          return x.toFixed(p);
        },
        "g": function g(x, p) {
          return x.toPrecision(p);
        },
        "o": function o(x) {
          return Math.round(x).toString(8);
        },
        "p": function p(x, _p) {
          return formatRounded(x * 100, _p);
        },
        "r": formatRounded,
        "s": formatPrefixAuto,
        "X": function X(x) {
          return Math.round(x).toString(16).toUpperCase();
        },
        "x": function x(_x) {
          return Math.round(_x).toString(16);
        }
      };

      // [[fill]align][sign][symbol][0][width][,][.precision][type]
      var re = /^(?:(.)?([<>=^]))?([+\-\( ])?([$#])?(0)?(\d+)?(,)?(\.\d+)?([a-z%])?$/i;

      function formatSpecifier(specifier) {
        return new FormatSpecifier(specifier);
      }

      formatSpecifier.prototype = FormatSpecifier.prototype; // instanceof

      function FormatSpecifier(specifier) {
        if (!(match = re.exec(specifier))) throw new Error("invalid format: " + specifier);

        var match,
            fill = match[1] || " ",
            align = match[2] || ">",
            sign = match[3] || "-",
            symbol = match[4] || "",
            zero = !!match[5],
            width = match[6] && +match[6],
            comma = !!match[7],
            precision = match[8] && +match[8].slice(1),
            type = match[9] || "";

        // The "n" type is an alias for ",g".
        if (type === "n") comma = true, type = "g";

        // Map invalid types to the default format.
        else if (!formatTypes[type]) type = "";

        // If zero fill is specified, padding goes after sign and before digits.
        if (zero || fill === "0" && align === "=") zero = true, fill = "0", align = "=";

        this.fill = fill;
        this.align = align;
        this.sign = sign;
        this.symbol = symbol;
        this.zero = zero;
        this.width = width;
        this.comma = comma;
        this.precision = precision;
        this.type = type;
      }

      FormatSpecifier.prototype.toString = function () {
        return this.fill + this.align + this.sign + this.symbol + (this.zero ? "0" : "") + (this.width == null ? "" : Math.max(1, this.width | 0)) + (this.comma ? "," : "") + (this.precision == null ? "" : "." + Math.max(0, this.precision | 0)) + this.type;
      };

      var identity = function identity(x) {
        return x;
      };

      var prefixes = ["y", "z", "a", "f", "p", "n", "µ", "m", "", "k", "M", "G", "T", "P", "E", "Z", "Y"];

      var formatLocale = function formatLocale(locale) {
        var group = locale.grouping && locale.thousands ? formatGroup(locale.grouping, locale.thousands) : identity,
            currency = locale.currency,
            decimal = locale.decimal,
            numerals = locale.numerals ? formatNumerals(locale.numerals) : identity,
            percent = locale.percent || "%";

        function newFormat(specifier) {
          specifier = formatSpecifier(specifier);

          var fill = specifier.fill,
              align = specifier.align,
              sign = specifier.sign,
              symbol = specifier.symbol,
              zero = specifier.zero,
              width = specifier.width,
              comma = specifier.comma,
              precision = specifier.precision,
              type = specifier.type;

          // Compute the prefix and suffix.
          // For SI-prefix, the suffix is lazily computed.
          var prefix = symbol === "$" ? currency[0] : symbol === "#" && /[boxX]/.test(type) ? "0" + type.toLowerCase() : "",
              suffix = symbol === "$" ? currency[1] : /[%p]/.test(type) ? percent : "";

          // What format function should we use?
          // Is this an integer type?
          // Can this type generate exponential notation?
          var formatType = formatTypes[type],
              maybeSuffix = !type || /[defgprs%]/.test(type);

          // Set the default precision if not specified,
          // or clamp the specified precision to the supported range.
          // For significant precision, it must be in [1, 21].
          // For fixed precision, it must be in [0, 20].
          precision = precision == null ? type ? 6 : 12 : /[gprs]/.test(type) ? Math.max(1, Math.min(21, precision)) : Math.max(0, Math.min(20, precision));

          function format(value) {
            var valuePrefix = prefix,
                valueSuffix = suffix,
                i,
                n,
                c;

            if (type === "c") {
              valueSuffix = formatType(value) + valueSuffix;
              value = "";
            } else {
              value = +value;

              // Perform the initial formatting.
              var valueNegative = value < 0;
              value = formatType(Math.abs(value), precision);

              // If a negative value rounds to zero during formatting, treat as positive.
              if (valueNegative && +value === 0) valueNegative = false;

              // Compute the prefix and suffix.
              valuePrefix = (valueNegative ? sign === "(" ? sign : "-" : sign === "-" || sign === "(" ? "" : sign) + valuePrefix;
              valueSuffix = valueSuffix + (type === "s" ? prefixes[8 + prefixExponent / 3] : "") + (valueNegative && sign === "(" ? ")" : "");

              // Break the formatted value into the integer “value” part that can be
              // grouped, and fractional or exponential “suffix” part that is not.
              if (maybeSuffix) {
                i = -1, n = value.length;
                while (++i < n) {
                  if (c = value.charCodeAt(i), 48 > c || c > 57) {
                    valueSuffix = (c === 46 ? decimal + value.slice(i + 1) : value.slice(i)) + valueSuffix;
                    value = value.slice(0, i);
                    break;
                  }
                }
              }
            }

            // If the fill character is not "0", grouping is applied before padding.
            if (comma && !zero) value = group(value, Infinity);

            // Compute the padding.
            var length = valuePrefix.length + value.length + valueSuffix.length,
                padding = length < width ? new Array(width - length + 1).join(fill) : "";

            // If the fill character is "0", grouping is applied after padding.
            if (comma && zero) value = group(padding + value, padding.length ? width - valueSuffix.length : Infinity), padding = "";

            // Reconstruct the final output based on the desired alignment.
            switch (align) {
              case "<":
                value = valuePrefix + value + valueSuffix + padding;break;
              case "=":
                value = valuePrefix + padding + value + valueSuffix;break;
              case "^":
                value = padding.slice(0, length = padding.length >> 1) + valuePrefix + value + valueSuffix + padding.slice(length);break;
              default:
                value = padding + valuePrefix + value + valueSuffix;break;
            }

            return numerals(value);
          }

          format.toString = function () {
            return specifier + "";
          };

          return format;
        }

        function formatPrefix(specifier, value) {
          var f = newFormat((specifier = formatSpecifier(specifier), specifier.type = "f", specifier)),
              e = Math.max(-8, Math.min(8, Math.floor(exponent(value) / 3))) * 3,
              k = Math.pow(10, -e),
              prefix = prefixes[8 + e / 3];
          return function (value) {
            return f(k * value) + prefix;
          };
        }

        return {
          format: newFormat,
          formatPrefix: formatPrefix
        };
      };

      var locale;

      defaultLocale({
        decimal: ".",
        thousands: ",",
        grouping: [3],
        currency: ["$", ""]
      });

      function defaultLocale(definition) {
        locale = formatLocale(definition);
        exports.format = locale.format;
        exports.formatPrefix = locale.formatPrefix;
        return locale;
      }

      var precisionFixed = function precisionFixed(step) {
        return Math.max(0, -exponent(Math.abs(step)));
      };

      var precisionPrefix = function precisionPrefix(step, value) {
        return Math.max(0, Math.max(-8, Math.min(8, Math.floor(exponent(value) / 3))) * 3 - exponent(Math.abs(step)));
      };

      var precisionRound = function precisionRound(step, max) {
        step = Math.abs(step), max = Math.abs(max) - step;
        return Math.max(0, exponent(max) - exponent(step)) + 1;
      };

      exports.formatDefaultLocale = defaultLocale;
      exports.formatLocale = formatLocale;
      exports.formatSpecifier = formatSpecifier;
      exports.precisionFixed = precisionFixed;
      exports.precisionPrefix = precisionPrefix;
      exports.precisionRound = precisionRound;

      Object.defineProperty(exports, '__esModule', { value: true });
    });
  }, {}], 6: [function (require, module, exports) {
    // https://d3js.org/d3-interpolate/ Version 1.1.5. Copyright 2017 Mike Bostock.
    (function (global, factory) {
      (typeof exports === "undefined" ? "undefined" : _typeof(exports)) === 'object' && typeof module !== 'undefined' ? factory(exports, require('d3-color')) : typeof define === 'function' && define.amd ? define(['exports', 'd3-color'], factory) : factory(global.d3 = global.d3 || {}, global.d3);
    })(this, function (exports, d3Color) {
      'use strict';

      function basis(t1, v0, v1, v2, v3) {
        var t2 = t1 * t1,
            t3 = t2 * t1;
        return ((1 - 3 * t1 + 3 * t2 - t3) * v0 + (4 - 6 * t2 + 3 * t3) * v1 + (1 + 3 * t1 + 3 * t2 - 3 * t3) * v2 + t3 * v3) / 6;
      }

      var basis$1 = function basis$1(values) {
        var n = values.length - 1;
        return function (t) {
          var i = t <= 0 ? t = 0 : t >= 1 ? (t = 1, n - 1) : Math.floor(t * n),
              v1 = values[i],
              v2 = values[i + 1],
              v0 = i > 0 ? values[i - 1] : 2 * v1 - v2,
              v3 = i < n - 1 ? values[i + 2] : 2 * v2 - v1;
          return basis((t - i / n) * n, v0, v1, v2, v3);
        };
      };

      var basisClosed = function basisClosed(values) {
        var n = values.length;
        return function (t) {
          var i = Math.floor(((t %= 1) < 0 ? ++t : t) * n),
              v0 = values[(i + n - 1) % n],
              v1 = values[i % n],
              v2 = values[(i + 1) % n],
              v3 = values[(i + 2) % n];
          return basis((t - i / n) * n, v0, v1, v2, v3);
        };
      };

      var constant = function constant(x) {
        return function () {
          return x;
        };
      };

      function linear(a, d) {
        return function (t) {
          return a + t * d;
        };
      }

      function exponential(a, b, y) {
        return a = Math.pow(a, y), b = Math.pow(b, y) - a, y = 1 / y, function (t) {
          return Math.pow(a + t * b, y);
        };
      }

      function hue(a, b) {
        var d = b - a;
        return d ? linear(a, d > 180 || d < -180 ? d - 360 * Math.round(d / 360) : d) : constant(isNaN(a) ? b : a);
      }

      function gamma(y) {
        return (y = +y) === 1 ? nogamma : function (a, b) {
          return b - a ? exponential(a, b, y) : constant(isNaN(a) ? b : a);
        };
      }

      function nogamma(a, b) {
        var d = b - a;
        return d ? linear(a, d) : constant(isNaN(a) ? b : a);
      }

      var rgb$1 = function rgbGamma(y) {
        var color$$1 = gamma(y);

        function rgb$$1(start, end) {
          var r = color$$1((start = d3Color.rgb(start)).r, (end = d3Color.rgb(end)).r),
              g = color$$1(start.g, end.g),
              b = color$$1(start.b, end.b),
              opacity = nogamma(start.opacity, end.opacity);
          return function (t) {
            start.r = r(t);
            start.g = g(t);
            start.b = b(t);
            start.opacity = opacity(t);
            return start + "";
          };
        }

        rgb$$1.gamma = rgbGamma;

        return rgb$$1;
      }(1);

      function rgbSpline(spline) {
        return function (colors) {
          var n = colors.length,
              r = new Array(n),
              g = new Array(n),
              b = new Array(n),
              i,
              color$$1;
          for (i = 0; i < n; ++i) {
            color$$1 = d3Color.rgb(colors[i]);
            r[i] = color$$1.r || 0;
            g[i] = color$$1.g || 0;
            b[i] = color$$1.b || 0;
          }
          r = spline(r);
          g = spline(g);
          b = spline(b);
          color$$1.opacity = 1;
          return function (t) {
            color$$1.r = r(t);
            color$$1.g = g(t);
            color$$1.b = b(t);
            return color$$1 + "";
          };
        };
      }

      var rgbBasis = rgbSpline(basis$1);
      var rgbBasisClosed = rgbSpline(basisClosed);

      var array = function array(a, b) {
        var nb = b ? b.length : 0,
            na = a ? Math.min(nb, a.length) : 0,
            x = new Array(nb),
            c = new Array(nb),
            i;

        for (i = 0; i < na; ++i) {
          x[i] = value(a[i], b[i]);
        }for (; i < nb; ++i) {
          c[i] = b[i];
        }return function (t) {
          for (i = 0; i < na; ++i) {
            c[i] = x[i](t);
          }return c;
        };
      };

      var date = function date(a, b) {
        var d = new Date();
        return a = +a, b -= a, function (t) {
          return d.setTime(a + b * t), d;
        };
      };

      var number = function number(a, b) {
        return a = +a, b -= a, function (t) {
          return a + b * t;
        };
      };

      var object = function object(a, b) {
        var i = {},
            c = {},
            k;

        if (a === null || (typeof a === "undefined" ? "undefined" : _typeof(a)) !== "object") a = {};
        if (b === null || (typeof b === "undefined" ? "undefined" : _typeof(b)) !== "object") b = {};

        for (k in b) {
          if (k in a) {
            i[k] = value(a[k], b[k]);
          } else {
            c[k] = b[k];
          }
        }

        return function (t) {
          for (k in i) {
            c[k] = i[k](t);
          }return c;
        };
      };

      var reA = /[-+]?(?:\d+\.?\d*|\.?\d+)(?:[eE][-+]?\d+)?/g;
      var reB = new RegExp(reA.source, "g");

      function zero(b) {
        return function () {
          return b;
        };
      }

      function one(b) {
        return function (t) {
          return b(t) + "";
        };
      }

      var string = function string(a, b) {
        var bi = reA.lastIndex = reB.lastIndex = 0,
            // scan index for next number in b
        am,
            // current match in a
        bm,
            // current match in b
        bs,
            // string preceding current number in b, if any
        i = -1,
            // index in s
        s = [],
            // string constants and placeholders
        q = []; // number interpolators

        // Coerce inputs to strings.
        a = a + "", b = b + "";

        // Interpolate pairs of numbers in a & b.
        while ((am = reA.exec(a)) && (bm = reB.exec(b))) {
          if ((bs = bm.index) > bi) {
            // a string precedes the next number in b
            bs = b.slice(bi, bs);
            if (s[i]) s[i] += bs; // coalesce with previous string
            else s[++i] = bs;
          }
          if ((am = am[0]) === (bm = bm[0])) {
            // numbers in a & b match
            if (s[i]) s[i] += bm; // coalesce with previous string
            else s[++i] = bm;
          } else {
            // interpolate non-matching numbers
            s[++i] = null;
            q.push({ i: i, x: number(am, bm) });
          }
          bi = reB.lastIndex;
        }

        // Add remains of b.
        if (bi < b.length) {
          bs = b.slice(bi);
          if (s[i]) s[i] += bs; // coalesce with previous string
          else s[++i] = bs;
        }

        // Special optimization for only a single match.
        // Otherwise, interpolate each of the numbers and rejoin the string.
        return s.length < 2 ? q[0] ? one(q[0].x) : zero(b) : (b = q.length, function (t) {
          for (var i = 0, o; i < b; ++i) {
            s[(o = q[i]).i] = o.x(t);
          }return s.join("");
        });
      };

      var value = function value(a, b) {
        var t = typeof b === "undefined" ? "undefined" : _typeof(b),
            c;
        return b == null || t === "boolean" ? constant(b) : (t === "number" ? number : t === "string" ? (c = d3Color.color(b)) ? (b = c, rgb$1) : string : b instanceof d3Color.color ? rgb$1 : b instanceof Date ? date : Array.isArray(b) ? array : typeof b.valueOf !== "function" && typeof b.toString !== "function" || isNaN(b) ? object : number)(a, b);
      };

      var round = function round(a, b) {
        return a = +a, b -= a, function (t) {
          return Math.round(a + b * t);
        };
      };

      var degrees = 180 / Math.PI;

      var identity = {
        translateX: 0,
        translateY: 0,
        rotate: 0,
        skewX: 0,
        scaleX: 1,
        scaleY: 1
      };

      var decompose = function decompose(a, b, c, d, e, f) {
        var scaleX, scaleY, skewX;
        if (scaleX = Math.sqrt(a * a + b * b)) a /= scaleX, b /= scaleX;
        if (skewX = a * c + b * d) c -= a * skewX, d -= b * skewX;
        if (scaleY = Math.sqrt(c * c + d * d)) c /= scaleY, d /= scaleY, skewX /= scaleY;
        if (a * d < b * c) a = -a, b = -b, skewX = -skewX, scaleX = -scaleX;
        return {
          translateX: e,
          translateY: f,
          rotate: Math.atan2(b, a) * degrees,
          skewX: Math.atan(skewX) * degrees,
          scaleX: scaleX,
          scaleY: scaleY
        };
      };

      var cssNode;
      var cssRoot;
      var cssView;
      var svgNode;

      function parseCss(value) {
        if (value === "none") return identity;
        if (!cssNode) cssNode = document.createElement("DIV"), cssRoot = document.documentElement, cssView = document.defaultView;
        cssNode.style.transform = value;
        value = cssView.getComputedStyle(cssRoot.appendChild(cssNode), null).getPropertyValue("transform");
        cssRoot.removeChild(cssNode);
        value = value.slice(7, -1).split(",");
        return decompose(+value[0], +value[1], +value[2], +value[3], +value[4], +value[5]);
      }

      function parseSvg(value) {
        if (value == null) return identity;
        if (!svgNode) svgNode = document.createElementNS("http://www.w3.org/2000/svg", "g");
        svgNode.setAttribute("transform", value);
        if (!(value = svgNode.transform.baseVal.consolidate())) return identity;
        value = value.matrix;
        return decompose(value.a, value.b, value.c, value.d, value.e, value.f);
      }

      function interpolateTransform(parse, pxComma, pxParen, degParen) {

        function pop(s) {
          return s.length ? s.pop() + " " : "";
        }

        function translate(xa, ya, xb, yb, s, q) {
          if (xa !== xb || ya !== yb) {
            var i = s.push("translate(", null, pxComma, null, pxParen);
            q.push({ i: i - 4, x: number(xa, xb) }, { i: i - 2, x: number(ya, yb) });
          } else if (xb || yb) {
            s.push("translate(" + xb + pxComma + yb + pxParen);
          }
        }

        function rotate(a, b, s, q) {
          if (a !== b) {
            if (a - b > 180) b += 360;else if (b - a > 180) a += 360; // shortest path
            q.push({ i: s.push(pop(s) + "rotate(", null, degParen) - 2, x: number(a, b) });
          } else if (b) {
            s.push(pop(s) + "rotate(" + b + degParen);
          }
        }

        function skewX(a, b, s, q) {
          if (a !== b) {
            q.push({ i: s.push(pop(s) + "skewX(", null, degParen) - 2, x: number(a, b) });
          } else if (b) {
            s.push(pop(s) + "skewX(" + b + degParen);
          }
        }

        function scale(xa, ya, xb, yb, s, q) {
          if (xa !== xb || ya !== yb) {
            var i = s.push(pop(s) + "scale(", null, ",", null, ")");
            q.push({ i: i - 4, x: number(xa, xb) }, { i: i - 2, x: number(ya, yb) });
          } else if (xb !== 1 || yb !== 1) {
            s.push(pop(s) + "scale(" + xb + "," + yb + ")");
          }
        }

        return function (a, b) {
          var s = [],
              // string constants and placeholders
          q = []; // number interpolators
          a = parse(a), b = parse(b);
          translate(a.translateX, a.translateY, b.translateX, b.translateY, s, q);
          rotate(a.rotate, b.rotate, s, q);
          skewX(a.skewX, b.skewX, s, q);
          scale(a.scaleX, a.scaleY, b.scaleX, b.scaleY, s, q);
          a = b = null; // gc
          return function (t) {
            var i = -1,
                n = q.length,
                o;
            while (++i < n) {
              s[(o = q[i]).i] = o.x(t);
            }return s.join("");
          };
        };
      }

      var interpolateTransformCss = interpolateTransform(parseCss, "px, ", "px)", "deg)");
      var interpolateTransformSvg = interpolateTransform(parseSvg, ", ", ")", ")");

      var rho = Math.SQRT2;
      var rho2 = 2;
      var rho4 = 4;
      var epsilon2 = 1e-12;

      function cosh(x) {
        return ((x = Math.exp(x)) + 1 / x) / 2;
      }

      function sinh(x) {
        return ((x = Math.exp(x)) - 1 / x) / 2;
      }

      function tanh(x) {
        return ((x = Math.exp(2 * x)) - 1) / (x + 1);
      }

      // p0 = [ux0, uy0, w0]
      // p1 = [ux1, uy1, w1]
      var zoom = function zoom(p0, p1) {
        var ux0 = p0[0],
            uy0 = p0[1],
            w0 = p0[2],
            ux1 = p1[0],
            uy1 = p1[1],
            w1 = p1[2],
            dx = ux1 - ux0,
            dy = uy1 - uy0,
            d2 = dx * dx + dy * dy,
            i,
            S;

        // Special case for u0 ≅ u1.
        if (d2 < epsilon2) {
          S = Math.log(w1 / w0) / rho;
          i = function i(t) {
            return [ux0 + t * dx, uy0 + t * dy, w0 * Math.exp(rho * t * S)];
          };
        }

        // General case.
        else {
            var d1 = Math.sqrt(d2),
                b0 = (w1 * w1 - w0 * w0 + rho4 * d2) / (2 * w0 * rho2 * d1),
                b1 = (w1 * w1 - w0 * w0 - rho4 * d2) / (2 * w1 * rho2 * d1),
                r0 = Math.log(Math.sqrt(b0 * b0 + 1) - b0),
                r1 = Math.log(Math.sqrt(b1 * b1 + 1) - b1);
            S = (r1 - r0) / rho;
            i = function i(t) {
              var s = t * S,
                  coshr0 = cosh(r0),
                  u = w0 / (rho2 * d1) * (coshr0 * tanh(rho * s + r0) - sinh(r0));
              return [ux0 + u * dx, uy0 + u * dy, w0 * coshr0 / cosh(rho * s + r0)];
            };
          }

        i.duration = S * 1000;

        return i;
      };

      function hsl$1(hue$$1) {
        return function (start, end) {
          var h = hue$$1((start = d3Color.hsl(start)).h, (end = d3Color.hsl(end)).h),
              s = nogamma(start.s, end.s),
              l = nogamma(start.l, end.l),
              opacity = nogamma(start.opacity, end.opacity);
          return function (t) {
            start.h = h(t);
            start.s = s(t);
            start.l = l(t);
            start.opacity = opacity(t);
            return start + "";
          };
        };
      }

      var hsl$2 = hsl$1(hue);
      var hslLong = hsl$1(nogamma);

      function lab$1(start, end) {
        var l = nogamma((start = d3Color.lab(start)).l, (end = d3Color.lab(end)).l),
            a = nogamma(start.a, end.a),
            b = nogamma(start.b, end.b),
            opacity = nogamma(start.opacity, end.opacity);
        return function (t) {
          start.l = l(t);
          start.a = a(t);
          start.b = b(t);
          start.opacity = opacity(t);
          return start + "";
        };
      }

      function hcl$1(hue$$1) {
        return function (start, end) {
          var h = hue$$1((start = d3Color.hcl(start)).h, (end = d3Color.hcl(end)).h),
              c = nogamma(start.c, end.c),
              l = nogamma(start.l, end.l),
              opacity = nogamma(start.opacity, end.opacity);
          return function (t) {
            start.h = h(t);
            start.c = c(t);
            start.l = l(t);
            start.opacity = opacity(t);
            return start + "";
          };
        };
      }

      var hcl$2 = hcl$1(hue);
      var hclLong = hcl$1(nogamma);

      function cubehelix$1(hue$$1) {
        return function cubehelixGamma(y) {
          y = +y;

          function cubehelix$$1(start, end) {
            var h = hue$$1((start = d3Color.cubehelix(start)).h, (end = d3Color.cubehelix(end)).h),
                s = nogamma(start.s, end.s),
                l = nogamma(start.l, end.l),
                opacity = nogamma(start.opacity, end.opacity);
            return function (t) {
              start.h = h(t);
              start.s = s(t);
              start.l = l(Math.pow(t, y));
              start.opacity = opacity(t);
              return start + "";
            };
          }

          cubehelix$$1.gamma = cubehelixGamma;

          return cubehelix$$1;
        }(1);
      }

      var cubehelix$2 = cubehelix$1(hue);
      var cubehelixLong = cubehelix$1(nogamma);

      var quantize = function quantize(interpolator, n) {
        var samples = new Array(n);
        for (var i = 0; i < n; ++i) {
          samples[i] = interpolator(i / (n - 1));
        }return samples;
      };

      exports.interpolate = value;
      exports.interpolateArray = array;
      exports.interpolateBasis = basis$1;
      exports.interpolateBasisClosed = basisClosed;
      exports.interpolateDate = date;
      exports.interpolateNumber = number;
      exports.interpolateObject = object;
      exports.interpolateRound = round;
      exports.interpolateString = string;
      exports.interpolateTransformCss = interpolateTransformCss;
      exports.interpolateTransformSvg = interpolateTransformSvg;
      exports.interpolateZoom = zoom;
      exports.interpolateRgb = rgb$1;
      exports.interpolateRgbBasis = rgbBasis;
      exports.interpolateRgbBasisClosed = rgbBasisClosed;
      exports.interpolateHsl = hsl$2;
      exports.interpolateHslLong = hslLong;
      exports.interpolateLab = lab$1;
      exports.interpolateHcl = hcl$2;
      exports.interpolateHclLong = hclLong;
      exports.interpolateCubehelix = cubehelix$2;
      exports.interpolateCubehelixLong = cubehelixLong;
      exports.quantize = quantize;

      Object.defineProperty(exports, '__esModule', { value: true });
    });
  }, { "d3-color": 3 }], 7: [function (require, module, exports) {
    // https://d3js.org/d3-scale/ Version 1.0.6. Copyright 2017 Mike Bostock.
    (function (global, factory) {
      (typeof exports === "undefined" ? "undefined" : _typeof(exports)) === 'object' && typeof module !== 'undefined' ? factory(exports, require('d3-array'), require('d3-collection'), require('d3-interpolate'), require('d3-format'), require('d3-time'), require('d3-time-format'), require('d3-color')) : typeof define === 'function' && define.amd ? define(['exports', 'd3-array', 'd3-collection', 'd3-interpolate', 'd3-format', 'd3-time', 'd3-time-format', 'd3-color'], factory) : factory(global.d3 = global.d3 || {}, global.d3, global.d3, global.d3, global.d3, global.d3, global.d3, global.d3);
    })(this, function (exports, d3Array, d3Collection, d3Interpolate, d3Format, d3Time, d3TimeFormat, d3Color) {
      'use strict';

      var array = Array.prototype;

      var map$1 = array.map;
      var slice = array.slice;

      var implicit = { name: "implicit" };

      function ordinal(range$$1) {
        var index = d3Collection.map(),
            domain = [],
            unknown = implicit;

        range$$1 = range$$1 == null ? [] : slice.call(range$$1);

        function scale(d) {
          var key = d + "",
              i = index.get(key);
          if (!i) {
            if (unknown !== implicit) return unknown;
            index.set(key, i = domain.push(d));
          }
          return range$$1[(i - 1) % range$$1.length];
        }

        scale.domain = function (_) {
          if (!arguments.length) return domain.slice();
          domain = [], index = d3Collection.map();
          var i = -1,
              n = _.length,
              d,
              key;
          while (++i < n) {
            if (!index.has(key = (d = _[i]) + "")) index.set(key, domain.push(d));
          }return scale;
        };

        scale.range = function (_) {
          return arguments.length ? (range$$1 = slice.call(_), scale) : range$$1.slice();
        };

        scale.unknown = function (_) {
          return arguments.length ? (unknown = _, scale) : unknown;
        };

        scale.copy = function () {
          return ordinal().domain(domain).range(range$$1).unknown(unknown);
        };

        return scale;
      }

      function band() {
        var scale = ordinal().unknown(undefined),
            domain = scale.domain,
            ordinalRange = scale.range,
            range$$1 = [0, 1],
            step,
            bandwidth,
            round = false,
            paddingInner = 0,
            paddingOuter = 0,
            align = 0.5;

        delete scale.unknown;

        function rescale() {
          var n = domain().length,
              reverse = range$$1[1] < range$$1[0],
              start = range$$1[reverse - 0],
              stop = range$$1[1 - reverse];
          step = (stop - start) / Math.max(1, n - paddingInner + paddingOuter * 2);
          if (round) step = Math.floor(step);
          start += (stop - start - step * (n - paddingInner)) * align;
          bandwidth = step * (1 - paddingInner);
          if (round) start = Math.round(start), bandwidth = Math.round(bandwidth);
          var values = d3Array.range(n).map(function (i) {
            return start + step * i;
          });
          return ordinalRange(reverse ? values.reverse() : values);
        }

        scale.domain = function (_) {
          return arguments.length ? (domain(_), rescale()) : domain();
        };

        scale.range = function (_) {
          return arguments.length ? (range$$1 = [+_[0], +_[1]], rescale()) : range$$1.slice();
        };

        scale.rangeRound = function (_) {
          return range$$1 = [+_[0], +_[1]], round = true, rescale();
        };

        scale.bandwidth = function () {
          return bandwidth;
        };

        scale.step = function () {
          return step;
        };

        scale.round = function (_) {
          return arguments.length ? (round = !!_, rescale()) : round;
        };

        scale.padding = function (_) {
          return arguments.length ? (paddingInner = paddingOuter = Math.max(0, Math.min(1, _)), rescale()) : paddingInner;
        };

        scale.paddingInner = function (_) {
          return arguments.length ? (paddingInner = Math.max(0, Math.min(1, _)), rescale()) : paddingInner;
        };

        scale.paddingOuter = function (_) {
          return arguments.length ? (paddingOuter = Math.max(0, Math.min(1, _)), rescale()) : paddingOuter;
        };

        scale.align = function (_) {
          return arguments.length ? (align = Math.max(0, Math.min(1, _)), rescale()) : align;
        };

        scale.copy = function () {
          return band().domain(domain()).range(range$$1).round(round).paddingInner(paddingInner).paddingOuter(paddingOuter).align(align);
        };

        return rescale();
      }

      function pointish(scale) {
        var copy = scale.copy;

        scale.padding = scale.paddingOuter;
        delete scale.paddingInner;
        delete scale.paddingOuter;

        scale.copy = function () {
          return pointish(copy());
        };

        return scale;
      }

      function point() {
        return pointish(band().paddingInner(1));
      }

      var constant = function constant(x) {
        return function () {
          return x;
        };
      };

      var number = function number(x) {
        return +x;
      };

      var unit = [0, 1];

      function deinterpolateLinear(a, b) {
        return (b -= a = +a) ? function (x) {
          return (x - a) / b;
        } : constant(b);
      }

      function deinterpolateClamp(deinterpolate) {
        return function (a, b) {
          var d = deinterpolate(a = +a, b = +b);
          return function (x) {
            return x <= a ? 0 : x >= b ? 1 : d(x);
          };
        };
      }

      function reinterpolateClamp(reinterpolate) {
        return function (a, b) {
          var r = reinterpolate(a = +a, b = +b);
          return function (t) {
            return t <= 0 ? a : t >= 1 ? b : r(t);
          };
        };
      }

      function bimap(domain, range$$1, deinterpolate, reinterpolate) {
        var d0 = domain[0],
            d1 = domain[1],
            r0 = range$$1[0],
            r1 = range$$1[1];
        if (d1 < d0) d0 = deinterpolate(d1, d0), r0 = reinterpolate(r1, r0);else d0 = deinterpolate(d0, d1), r0 = reinterpolate(r0, r1);
        return function (x) {
          return r0(d0(x));
        };
      }

      function polymap(domain, range$$1, deinterpolate, reinterpolate) {
        var j = Math.min(domain.length, range$$1.length) - 1,
            d = new Array(j),
            r = new Array(j),
            i = -1;

        // Reverse descending domains.
        if (domain[j] < domain[0]) {
          domain = domain.slice().reverse();
          range$$1 = range$$1.slice().reverse();
        }

        while (++i < j) {
          d[i] = deinterpolate(domain[i], domain[i + 1]);
          r[i] = reinterpolate(range$$1[i], range$$1[i + 1]);
        }

        return function (x) {
          var i = d3Array.bisect(domain, x, 1, j) - 1;
          return r[i](d[i](x));
        };
      }

      function copy(source, target) {
        return target.domain(source.domain()).range(source.range()).interpolate(source.interpolate()).clamp(source.clamp());
      }

      // deinterpolate(a, b)(x) takes a domain value x in [a,b] and returns the corresponding parameter t in [0,1].
      // reinterpolate(a, b)(t) takes a parameter t in [0,1] and returns the corresponding domain value x in [a,b].
      function continuous(deinterpolate, reinterpolate) {
        var domain = unit,
            range$$1 = unit,
            interpolate$$1 = d3Interpolate.interpolate,
            clamp = false,
            piecewise,
            output,
            input;

        function rescale() {
          piecewise = Math.min(domain.length, range$$1.length) > 2 ? polymap : bimap;
          output = input = null;
          return scale;
        }

        function scale(x) {
          return (output || (output = piecewise(domain, range$$1, clamp ? deinterpolateClamp(deinterpolate) : deinterpolate, interpolate$$1)))(+x);
        }

        scale.invert = function (y) {
          return (input || (input = piecewise(range$$1, domain, deinterpolateLinear, clamp ? reinterpolateClamp(reinterpolate) : reinterpolate)))(+y);
        };

        scale.domain = function (_) {
          return arguments.length ? (domain = map$1.call(_, number), rescale()) : domain.slice();
        };

        scale.range = function (_) {
          return arguments.length ? (range$$1 = slice.call(_), rescale()) : range$$1.slice();
        };

        scale.rangeRound = function (_) {
          return range$$1 = slice.call(_), interpolate$$1 = d3Interpolate.interpolateRound, rescale();
        };

        scale.clamp = function (_) {
          return arguments.length ? (clamp = !!_, rescale()) : clamp;
        };

        scale.interpolate = function (_) {
          return arguments.length ? (interpolate$$1 = _, rescale()) : interpolate$$1;
        };

        return rescale();
      }

      var tickFormat = function tickFormat(domain, count, specifier) {
        var start = domain[0],
            stop = domain[domain.length - 1],
            step = d3Array.tickStep(start, stop, count == null ? 10 : count),
            precision;
        specifier = d3Format.formatSpecifier(specifier == null ? ",f" : specifier);
        switch (specifier.type) {
          case "s":
            {
              var value = Math.max(Math.abs(start), Math.abs(stop));
              if (specifier.precision == null && !isNaN(precision = d3Format.precisionPrefix(step, value))) specifier.precision = precision;
              return d3Format.formatPrefix(specifier, value);
            }
          case "":
          case "e":
          case "g":
          case "p":
          case "r":
            {
              if (specifier.precision == null && !isNaN(precision = d3Format.precisionRound(step, Math.max(Math.abs(start), Math.abs(stop))))) specifier.precision = precision - (specifier.type === "e");
              break;
            }
          case "f":
          case "%":
            {
              if (specifier.precision == null && !isNaN(precision = d3Format.precisionFixed(step))) specifier.precision = precision - (specifier.type === "%") * 2;
              break;
            }
        }
        return d3Format.format(specifier);
      };

      function linearish(scale) {
        var domain = scale.domain;

        scale.ticks = function (count) {
          var d = domain();
          return d3Array.ticks(d[0], d[d.length - 1], count == null ? 10 : count);
        };

        scale.tickFormat = function (count, specifier) {
          return tickFormat(domain(), count, specifier);
        };

        scale.nice = function (count) {
          if (count == null) count = 10;

          var d = domain(),
              i0 = 0,
              i1 = d.length - 1,
              start = d[i0],
              stop = d[i1],
              step;

          if (stop < start) {
            step = start, start = stop, stop = step;
            step = i0, i0 = i1, i1 = step;
          }

          step = d3Array.tickIncrement(start, stop, count);

          if (step > 0) {
            start = Math.floor(start / step) * step;
            stop = Math.ceil(stop / step) * step;
            step = d3Array.tickIncrement(start, stop, count);
          } else if (step < 0) {
            start = Math.ceil(start * step) / step;
            stop = Math.floor(stop * step) / step;
            step = d3Array.tickIncrement(start, stop, count);
          }

          if (step > 0) {
            d[i0] = Math.floor(start / step) * step;
            d[i1] = Math.ceil(stop / step) * step;
            domain(d);
          } else if (step < 0) {
            d[i0] = Math.ceil(start * step) / step;
            d[i1] = Math.floor(stop * step) / step;
            domain(d);
          }

          return scale;
        };

        return scale;
      }

      function linear() {
        var scale = continuous(deinterpolateLinear, d3Interpolate.interpolateNumber);

        scale.copy = function () {
          return copy(scale, linear());
        };

        return linearish(scale);
      }

      function identity() {
        var domain = [0, 1];

        function scale(x) {
          return +x;
        }

        scale.invert = scale;

        scale.domain = scale.range = function (_) {
          return arguments.length ? (domain = map$1.call(_, number), scale) : domain.slice();
        };

        scale.copy = function () {
          return identity().domain(domain);
        };

        return linearish(scale);
      }

      var nice = function nice(domain, interval) {
        domain = domain.slice();

        var i0 = 0,
            i1 = domain.length - 1,
            x0 = domain[i0],
            x1 = domain[i1],
            t;

        if (x1 < x0) {
          t = i0, i0 = i1, i1 = t;
          t = x0, x0 = x1, x1 = t;
        }

        domain[i0] = interval.floor(x0);
        domain[i1] = interval.ceil(x1);
        return domain;
      };

      function deinterpolate(a, b) {
        return (b = Math.log(b / a)) ? function (x) {
          return Math.log(x / a) / b;
        } : constant(b);
      }

      function reinterpolate(a, b) {
        return a < 0 ? function (t) {
          return -Math.pow(-b, t) * Math.pow(-a, 1 - t);
        } : function (t) {
          return Math.pow(b, t) * Math.pow(a, 1 - t);
        };
      }

      function pow10(x) {
        return isFinite(x) ? +("1e" + x) : x < 0 ? 0 : x;
      }

      function powp(base) {
        return base === 10 ? pow10 : base === Math.E ? Math.exp : function (x) {
          return Math.pow(base, x);
        };
      }

      function logp(base) {
        return base === Math.E ? Math.log : base === 10 && Math.log10 || base === 2 && Math.log2 || (base = Math.log(base), function (x) {
          return Math.log(x) / base;
        });
      }

      function reflect(f) {
        return function (x) {
          return -f(-x);
        };
      }

      function log() {
        var scale = continuous(deinterpolate, reinterpolate).domain([1, 10]),
            domain = scale.domain,
            base = 10,
            logs = logp(10),
            pows = powp(10);

        function rescale() {
          logs = logp(base), pows = powp(base);
          if (domain()[0] < 0) logs = reflect(logs), pows = reflect(pows);
          return scale;
        }

        scale.base = function (_) {
          return arguments.length ? (base = +_, rescale()) : base;
        };

        scale.domain = function (_) {
          return arguments.length ? (domain(_), rescale()) : domain();
        };

        scale.ticks = function (count) {
          var d = domain(),
              u = d[0],
              v = d[d.length - 1],
              r;

          if (r = v < u) i = u, u = v, v = i;

          var i = logs(u),
              j = logs(v),
              p,
              k,
              t,
              n = count == null ? 10 : +count,
              z = [];

          if (!(base % 1) && j - i < n) {
            i = Math.round(i) - 1, j = Math.round(j) + 1;
            if (u > 0) for (; i < j; ++i) {
              for (k = 1, p = pows(i); k < base; ++k) {
                t = p * k;
                if (t < u) continue;
                if (t > v) break;
                z.push(t);
              }
            } else for (; i < j; ++i) {
              for (k = base - 1, p = pows(i); k >= 1; --k) {
                t = p * k;
                if (t < u) continue;
                if (t > v) break;
                z.push(t);
              }
            }
          } else {
            z = d3Array.ticks(i, j, Math.min(j - i, n)).map(pows);
          }

          return r ? z.reverse() : z;
        };

        scale.tickFormat = function (count, specifier) {
          if (specifier == null) specifier = base === 10 ? ".0e" : ",";
          if (typeof specifier !== "function") specifier = d3Format.format(specifier);
          if (count === Infinity) return specifier;
          if (count == null) count = 10;
          var k = Math.max(1, base * count / scale.ticks().length); // TODO fast estimate?
          return function (d) {
            var i = d / pows(Math.round(logs(d)));
            if (i * base < base - 0.5) i *= base;
            return i <= k ? specifier(d) : "";
          };
        };

        scale.nice = function () {
          return domain(nice(domain(), {
            floor: function floor(x) {
              return pows(Math.floor(logs(x)));
            },
            ceil: function ceil(x) {
              return pows(Math.ceil(logs(x)));
            }
          }));
        };

        scale.copy = function () {
          return copy(scale, log().base(base));
        };

        return scale;
      }

      function raise(x, exponent) {
        return x < 0 ? -Math.pow(-x, exponent) : Math.pow(x, exponent);
      }

      function pow() {
        var exponent = 1,
            scale = continuous(deinterpolate, reinterpolate),
            domain = scale.domain;

        function deinterpolate(a, b) {
          return (b = raise(b, exponent) - (a = raise(a, exponent))) ? function (x) {
            return (raise(x, exponent) - a) / b;
          } : constant(b);
        }

        function reinterpolate(a, b) {
          b = raise(b, exponent) - (a = raise(a, exponent));
          return function (t) {
            return raise(a + b * t, 1 / exponent);
          };
        }

        scale.exponent = function (_) {
          return arguments.length ? (exponent = +_, domain(domain())) : exponent;
        };

        scale.copy = function () {
          return copy(scale, pow().exponent(exponent));
        };

        return linearish(scale);
      }

      function sqrt() {
        return pow().exponent(0.5);
      }

      function quantile$1() {
        var domain = [],
            range$$1 = [],
            thresholds = [];

        function rescale() {
          var i = 0,
              n = Math.max(1, range$$1.length);
          thresholds = new Array(n - 1);
          while (++i < n) {
            thresholds[i - 1] = d3Array.quantile(domain, i / n);
          }return scale;
        }

        function scale(x) {
          if (!isNaN(x = +x)) return range$$1[d3Array.bisect(thresholds, x)];
        }

        scale.invertExtent = function (y) {
          var i = range$$1.indexOf(y);
          return i < 0 ? [NaN, NaN] : [i > 0 ? thresholds[i - 1] : domain[0], i < thresholds.length ? thresholds[i] : domain[domain.length - 1]];
        };

        scale.domain = function (_) {
          if (!arguments.length) return domain.slice();
          domain = [];
          for (var i = 0, n = _.length, d; i < n; ++i) {
            if (d = _[i], d != null && !isNaN(d = +d)) domain.push(d);
          }domain.sort(d3Array.ascending);
          return rescale();
        };

        scale.range = function (_) {
          return arguments.length ? (range$$1 = slice.call(_), rescale()) : range$$1.slice();
        };

        scale.quantiles = function () {
          return thresholds.slice();
        };

        scale.copy = function () {
          return quantile$1().domain(domain).range(range$$1);
        };

        return scale;
      }

      function quantize() {
        var x0 = 0,
            x1 = 1,
            n = 1,
            domain = [0.5],
            range$$1 = [0, 1];

        function scale(x) {
          if (x <= x) return range$$1[d3Array.bisect(domain, x, 0, n)];
        }

        function rescale() {
          var i = -1;
          domain = new Array(n);
          while (++i < n) {
            domain[i] = ((i + 1) * x1 - (i - n) * x0) / (n + 1);
          }return scale;
        }

        scale.domain = function (_) {
          return arguments.length ? (x0 = +_[0], x1 = +_[1], rescale()) : [x0, x1];
        };

        scale.range = function (_) {
          return arguments.length ? (n = (range$$1 = slice.call(_)).length - 1, rescale()) : range$$1.slice();
        };

        scale.invertExtent = function (y) {
          var i = range$$1.indexOf(y);
          return i < 0 ? [NaN, NaN] : i < 1 ? [x0, domain[0]] : i >= n ? [domain[n - 1], x1] : [domain[i - 1], domain[i]];
        };

        scale.copy = function () {
          return quantize().domain([x0, x1]).range(range$$1);
        };

        return linearish(scale);
      }

      function threshold() {
        var domain = [0.5],
            range$$1 = [0, 1],
            n = 1;

        function scale(x) {
          if (x <= x) return range$$1[d3Array.bisect(domain, x, 0, n)];
        }

        scale.domain = function (_) {
          return arguments.length ? (domain = slice.call(_), n = Math.min(domain.length, range$$1.length - 1), scale) : domain.slice();
        };

        scale.range = function (_) {
          return arguments.length ? (range$$1 = slice.call(_), n = Math.min(domain.length, range$$1.length - 1), scale) : range$$1.slice();
        };

        scale.invertExtent = function (y) {
          var i = range$$1.indexOf(y);
          return [domain[i - 1], domain[i]];
        };

        scale.copy = function () {
          return threshold().domain(domain).range(range$$1);
        };

        return scale;
      }

      var durationSecond = 1000;
      var durationMinute = durationSecond * 60;
      var durationHour = durationMinute * 60;
      var durationDay = durationHour * 24;
      var durationWeek = durationDay * 7;
      var durationMonth = durationDay * 30;
      var durationYear = durationDay * 365;

      function date(t) {
        return new Date(t);
      }

      function number$1(t) {
        return t instanceof Date ? +t : +new Date(+t);
      }

      function calendar(year, month, week, day, hour, minute, second, millisecond, format$$1) {
        var scale = continuous(deinterpolateLinear, d3Interpolate.interpolateNumber),
            invert = scale.invert,
            domain = scale.domain;

        var formatMillisecond = format$$1(".%L"),
            formatSecond = format$$1(":%S"),
            formatMinute = format$$1("%I:%M"),
            formatHour = format$$1("%I %p"),
            formatDay = format$$1("%a %d"),
            formatWeek = format$$1("%b %d"),
            formatMonth = format$$1("%B"),
            formatYear = format$$1("%Y");

        var tickIntervals = [[second, 1, durationSecond], [second, 5, 5 * durationSecond], [second, 15, 15 * durationSecond], [second, 30, 30 * durationSecond], [minute, 1, durationMinute], [minute, 5, 5 * durationMinute], [minute, 15, 15 * durationMinute], [minute, 30, 30 * durationMinute], [hour, 1, durationHour], [hour, 3, 3 * durationHour], [hour, 6, 6 * durationHour], [hour, 12, 12 * durationHour], [day, 1, durationDay], [day, 2, 2 * durationDay], [week, 1, durationWeek], [month, 1, durationMonth], [month, 3, 3 * durationMonth], [year, 1, durationYear]];

        function tickFormat(date) {
          return (second(date) < date ? formatMillisecond : minute(date) < date ? formatSecond : hour(date) < date ? formatMinute : day(date) < date ? formatHour : month(date) < date ? week(date) < date ? formatDay : formatWeek : year(date) < date ? formatMonth : formatYear)(date);
        }

        function tickInterval(interval, start, stop, step) {
          if (interval == null) interval = 10;

          // If a desired tick count is specified, pick a reasonable tick interval
          // based on the extent of the domain and a rough estimate of tick size.
          // Otherwise, assume interval is already a time interval and use it.
          if (typeof interval === "number") {
            var target = Math.abs(stop - start) / interval,
                i = d3Array.bisector(function (i) {
              return i[2];
            }).right(tickIntervals, target);
            if (i === tickIntervals.length) {
              step = d3Array.tickStep(start / durationYear, stop / durationYear, interval);
              interval = year;
            } else if (i) {
              i = tickIntervals[target / tickIntervals[i - 1][2] < tickIntervals[i][2] / target ? i - 1 : i];
              step = i[1];
              interval = i[0];
            } else {
              step = d3Array.tickStep(start, stop, interval);
              interval = millisecond;
            }
          }

          return step == null ? interval : interval.every(step);
        }

        scale.invert = function (y) {
          return new Date(invert(y));
        };

        scale.domain = function (_) {
          return arguments.length ? domain(map$1.call(_, number$1)) : domain().map(date);
        };

        scale.ticks = function (interval, step) {
          var d = domain(),
              t0 = d[0],
              t1 = d[d.length - 1],
              r = t1 < t0,
              t;
          if (r) t = t0, t0 = t1, t1 = t;
          t = tickInterval(interval, t0, t1, step);
          t = t ? t.range(t0, t1 + 1) : []; // inclusive stop
          return r ? t.reverse() : t;
        };

        scale.tickFormat = function (count, specifier) {
          return specifier == null ? tickFormat : format$$1(specifier);
        };

        scale.nice = function (interval, step) {
          var d = domain();
          return (interval = tickInterval(interval, d[0], d[d.length - 1], step)) ? domain(nice(d, interval)) : scale;
        };

        scale.copy = function () {
          return copy(scale, calendar(year, month, week, day, hour, minute, second, millisecond, format$$1));
        };

        return scale;
      }

      var time = function time() {
        return calendar(d3Time.timeYear, d3Time.timeMonth, d3Time.timeWeek, d3Time.timeDay, d3Time.timeHour, d3Time.timeMinute, d3Time.timeSecond, d3Time.timeMillisecond, d3TimeFormat.timeFormat).domain([new Date(2000, 0, 1), new Date(2000, 0, 2)]);
      };

      var utcTime = function utcTime() {
        return calendar(d3Time.utcYear, d3Time.utcMonth, d3Time.utcWeek, d3Time.utcDay, d3Time.utcHour, d3Time.utcMinute, d3Time.utcSecond, d3Time.utcMillisecond, d3TimeFormat.utcFormat).domain([Date.UTC(2000, 0, 1), Date.UTC(2000, 0, 2)]);
      };

      var colors = function colors(s) {
        return s.match(/.{6}/g).map(function (x) {
          return "#" + x;
        });
      };

      var category10 = colors("1f77b4ff7f0e2ca02cd627289467bd8c564be377c27f7f7fbcbd2217becf");

      var category20b = colors("393b795254a36b6ecf9c9ede6379398ca252b5cf6bcedb9c8c6d31bd9e39e7ba52e7cb94843c39ad494ad6616be7969c7b4173a55194ce6dbdde9ed6");

      var category20c = colors("3182bd6baed69ecae1c6dbefe6550dfd8d3cfdae6bfdd0a231a35474c476a1d99bc7e9c0756bb19e9ac8bcbddcdadaeb636363969696bdbdbdd9d9d9");

      var category20 = colors("1f77b4aec7e8ff7f0effbb782ca02c98df8ad62728ff98969467bdc5b0d58c564bc49c94e377c2f7b6d27f7f7fc7c7c7bcbd22dbdb8d17becf9edae5");

      var cubehelix$1 = d3Interpolate.interpolateCubehelixLong(d3Color.cubehelix(300, 0.5, 0.0), d3Color.cubehelix(-240, 0.5, 1.0));

      var warm = d3Interpolate.interpolateCubehelixLong(d3Color.cubehelix(-100, 0.75, 0.35), d3Color.cubehelix(80, 1.50, 0.8));

      var cool = d3Interpolate.interpolateCubehelixLong(d3Color.cubehelix(260, 0.75, 0.35), d3Color.cubehelix(80, 1.50, 0.8));

      var rainbow = d3Color.cubehelix();

      var rainbow$1 = function rainbow$1(t) {
        if (t < 0 || t > 1) t -= Math.floor(t);
        var ts = Math.abs(t - 0.5);
        rainbow.h = 360 * t - 100;
        rainbow.s = 1.5 - 1.5 * ts;
        rainbow.l = 0.8 - 0.9 * ts;
        return rainbow + "";
      };

      function ramp(range$$1) {
        var n = range$$1.length;
        return function (t) {
          return range$$1[Math.max(0, Math.min(n - 1, Math.floor(t * n)))];
        };
      }

      var viridis = ramp(colors("44015444025645045745055946075a46085c460a5d460b5e470d60470e6147106347116447136548146748166848176948186a481a6c481b6d481c6e481d6f481f70482071482173482374482475482576482677482878482979472a7a472c7a472d7b472e7c472f7d46307e46327e46337f463480453581453781453882443983443a83443b84433d84433e85423f854240864241864142874144874045884046883f47883f48893e49893e4a893e4c8a3d4d8a3d4e8a3c4f8a3c508b3b518b3b528b3a538b3a548c39558c39568c38588c38598c375a8c375b8d365c8d365d8d355e8d355f8d34608d34618d33628d33638d32648e32658e31668e31678e31688e30698e306a8e2f6b8e2f6c8e2e6d8e2e6e8e2e6f8e2d708e2d718e2c718e2c728e2c738e2b748e2b758e2a768e2a778e2a788e29798e297a8e297b8e287c8e287d8e277e8e277f8e27808e26818e26828e26828e25838e25848e25858e24868e24878e23888e23898e238a8d228b8d228c8d228d8d218e8d218f8d21908d21918c20928c20928c20938c1f948c1f958b1f968b1f978b1f988b1f998a1f9a8a1e9b8a1e9c891e9d891f9e891f9f881fa0881fa1881fa1871fa28720a38620a48621a58521a68522a78522a88423a98324aa8325ab8225ac8226ad8127ad8128ae8029af7f2ab07f2cb17e2db27d2eb37c2fb47c31b57b32b67a34b67935b77937b87838b9773aba763bbb753dbc743fbc7340bd7242be7144bf7046c06f48c16e4ac16d4cc26c4ec36b50c46a52c56954c56856c66758c7655ac8645cc8635ec96260ca6063cb5f65cb5e67cc5c69cd5b6ccd5a6ece5870cf5773d05675d05477d1537ad1517cd2507fd34e81d34d84d44b86d54989d5488bd6468ed64590d74393d74195d84098d83e9bd93c9dd93ba0da39a2da37a5db36a8db34aadc32addc30b0dd2fb2dd2db5de2bb8de29bade28bddf26c0df25c2df23c5e021c8e020cae11fcde11dd0e11cd2e21bd5e21ad8e219dae319dde318dfe318e2e418e5e419e7e419eae51aece51befe51cf1e51df4e61ef6e620f8e621fbe723fde725"));

      var magma = ramp(colors("00000401000501010601010802010902020b02020d03030f03031204041405041606051806051a07061c08071e0907200a08220b09240c09260d0a290e0b2b100b2d110c2f120d31130d34140e36150e38160f3b180f3d19103f1a10421c10441d11471e114920114b21114e22115024125325125527125829115a2a115c2c115f2d11612f116331116533106734106936106b38106c390f6e3b0f703d0f713f0f72400f74420f75440f764510774710784910784a10794c117a4e117b4f127b51127c52137c54137d56147d57157e59157e5a167e5c167f5d177f5f187f601880621980641a80651a80671b80681c816a1c816b1d816d1d816e1e81701f81721f817320817521817621817822817922827b23827c23827e24828025828125818326818426818627818827818928818b29818c29818e2a81902a81912b81932b80942c80962c80982d80992d809b2e7f9c2e7f9e2f7fa02f7fa1307ea3307ea5317ea6317da8327daa337dab337cad347cae347bb0357bb2357bb3367ab5367ab73779b83779ba3878bc3978bd3977bf3a77c03a76c23b75c43c75c53c74c73d73c83e73ca3e72cc3f71cd4071cf4070d0416fd2426fd3436ed5446dd6456cd8456cd9466bdb476adc4869de4968df4a68e04c67e24d66e34e65e44f64e55064e75263e85362e95462ea5661eb5760ec5860ed5a5fee5b5eef5d5ef05f5ef1605df2625df2645cf3655cf4675cf4695cf56b5cf66c5cf66e5cf7705cf7725cf8745cf8765cf9785df9795df97b5dfa7d5efa7f5efa815ffb835ffb8560fb8761fc8961fc8a62fc8c63fc8e64fc9065fd9266fd9467fd9668fd9869fd9a6afd9b6bfe9d6cfe9f6dfea16efea36ffea571fea772fea973feaa74feac76feae77feb078feb27afeb47bfeb67cfeb77efeb97ffebb81febd82febf84fec185fec287fec488fec68afec88cfeca8dfecc8ffecd90fecf92fed194fed395fed597fed799fed89afdda9cfddc9efddea0fde0a1fde2a3fde3a5fde5a7fde7a9fde9aafdebacfcecaefceeb0fcf0b2fcf2b4fcf4b6fcf6b8fcf7b9fcf9bbfcfbbdfcfdbf"));

      var inferno = ramp(colors("00000401000501010601010802010a02020c02020e03021004031204031405041706041907051b08051d09061f0a07220b07240c08260d08290e092b10092d110a30120a32140b34150b37160b39180c3c190c3e1b0c411c0c431e0c451f0c48210c4a230c4c240c4f260c51280b53290b552b0b572d0b592f0a5b310a5c320a5e340a5f3609613809623909633b09643d09653e0966400a67420a68440a68450a69470b6a490b6a4a0c6b4c0c6b4d0d6c4f0d6c510e6c520e6d540f6d550f6d57106e59106e5a116e5c126e5d126e5f136e61136e62146e64156e65156e67166e69166e6a176e6c186e6d186e6f196e71196e721a6e741a6e751b6e771c6d781c6d7a1d6d7c1d6d7d1e6d7f1e6c801f6c82206c84206b85216b87216b88226a8a226a8c23698d23698f24699025689225689326679526679727669827669a28659b29649d29649f2a63a02a63a22b62a32c61a52c60a62d60a82e5fa92e5eab2f5ead305dae305cb0315bb1325ab3325ab43359b63458b73557b93556ba3655bc3754bd3853bf3952c03a51c13a50c33b4fc43c4ec63d4dc73e4cc83f4bca404acb4149cc4248ce4347cf4446d04545d24644d34743d44842d54a41d74b3fd84c3ed94d3dda4e3cdb503bdd513ade5238df5337e05536e15635e25734e35933e45a31e55c30e65d2fe75e2ee8602de9612bea632aeb6429eb6628ec6726ed6925ee6a24ef6c23ef6e21f06f20f1711ff1731df2741cf3761bf37819f47918f57b17f57d15f67e14f68013f78212f78410f8850ff8870ef8890cf98b0bf98c0af98e09fa9008fa9207fa9407fb9606fb9706fb9906fb9b06fb9d07fc9f07fca108fca309fca50afca60cfca80dfcaa0ffcac11fcae12fcb014fcb216fcb418fbb61afbb81dfbba1ffbbc21fbbe23fac026fac228fac42afac62df9c72ff9c932f9cb35f8cd37f8cf3af7d13df7d340f6d543f6d746f5d949f5db4cf4dd4ff4df53f4e156f3e35af3e55df2e661f2e865f2ea69f1ec6df1ed71f1ef75f1f179f2f27df2f482f3f586f3f68af4f88ef5f992f6fa96f8fb9af9fc9dfafda1fcffa4"));

      var plasma = ramp(colors("0d088710078813078916078a19068c1b068d1d068e20068f2206902406912605912805922a05932c05942e05952f059631059733059735049837049938049a3a049a3c049b3e049c3f049c41049d43039e44039e46039f48039f4903a04b03a14c02a14e02a25002a25102a35302a35502a45601a45801a45901a55b01a55c01a65e01a66001a66100a76300a76400a76600a76700a86900a86a00a86c00a86e00a86f00a87100a87201a87401a87501a87701a87801a87a02a87b02a87d03a87e03a88004a88104a78305a78405a78606a68707a68808a68a09a58b0aa58d0ba58e0ca48f0da4910ea3920fa39410a29511a19613a19814a099159f9a169f9c179e9d189d9e199da01a9ca11b9ba21d9aa31e9aa51f99a62098a72197a82296aa2395ab2494ac2694ad2793ae2892b02991b12a90b22b8fb32c8eb42e8db52f8cb6308bb7318ab83289ba3388bb3488bc3587bd3786be3885bf3984c03a83c13b82c23c81c33d80c43e7fc5407ec6417dc7427cc8437bc9447aca457acb4679cc4778cc4977cd4a76ce4b75cf4c74d04d73d14e72d24f71d35171d45270d5536fd5546ed6556dd7566cd8576bd9586ada5a6ada5b69db5c68dc5d67dd5e66de5f65de6164df6263e06363e16462e26561e26660e3685fe4695ee56a5de56b5de66c5ce76e5be76f5ae87059e97158e97257ea7457eb7556eb7655ec7754ed7953ed7a52ee7b51ef7c51ef7e50f07f4ff0804ef1814df1834cf2844bf3854bf3874af48849f48948f58b47f58c46f68d45f68f44f79044f79143f79342f89441f89540f9973ff9983ef99a3efa9b3dfa9c3cfa9e3bfb9f3afba139fba238fca338fca537fca636fca835fca934fdab33fdac33fdae32fdaf31fdb130fdb22ffdb42ffdb52efeb72dfeb82cfeba2cfebb2bfebd2afebe2afec029fdc229fdc328fdc527fdc627fdc827fdca26fdcb26fccd25fcce25fcd025fcd225fbd324fbd524fbd724fad824fada24f9dc24f9dd25f8df25f8e125f7e225f7e425f6e626f6e826f5e926f5eb27f4ed27f3ee27f3f027f2f227f1f426f1f525f0f724f0f921"));

      function sequential(interpolator) {
        var x0 = 0,
            x1 = 1,
            clamp = false;

        function scale(x) {
          var t = (x - x0) / (x1 - x0);
          return interpolator(clamp ? Math.max(0, Math.min(1, t)) : t);
        }

        scale.domain = function (_) {
          return arguments.length ? (x0 = +_[0], x1 = +_[1], scale) : [x0, x1];
        };

        scale.clamp = function (_) {
          return arguments.length ? (clamp = !!_, scale) : clamp;
        };

        scale.interpolator = function (_) {
          return arguments.length ? (interpolator = _, scale) : interpolator;
        };

        scale.copy = function () {
          return sequential(interpolator).domain([x0, x1]).clamp(clamp);
        };

        return linearish(scale);
      }

      exports.scaleBand = band;
      exports.scalePoint = point;
      exports.scaleIdentity = identity;
      exports.scaleLinear = linear;
      exports.scaleLog = log;
      exports.scaleOrdinal = ordinal;
      exports.scaleImplicit = implicit;
      exports.scalePow = pow;
      exports.scaleSqrt = sqrt;
      exports.scaleQuantile = quantile$1;
      exports.scaleQuantize = quantize;
      exports.scaleThreshold = threshold;
      exports.scaleTime = time;
      exports.scaleUtc = utcTime;
      exports.schemeCategory10 = category10;
      exports.schemeCategory20b = category20b;
      exports.schemeCategory20c = category20c;
      exports.schemeCategory20 = category20;
      exports.interpolateCubehelixDefault = cubehelix$1;
      exports.interpolateRainbow = rainbow$1;
      exports.interpolateWarm = warm;
      exports.interpolateCool = cool;
      exports.interpolateViridis = viridis;
      exports.interpolateMagma = magma;
      exports.interpolateInferno = inferno;
      exports.interpolatePlasma = plasma;
      exports.scaleSequential = sequential;

      Object.defineProperty(exports, '__esModule', { value: true });
    });
  }, { "d3-array": 1, "d3-collection": 2, "d3-color": 3, "d3-format": 5, "d3-interpolate": 6, "d3-time": 9, "d3-time-format": 8 }], 8: [function (require, module, exports) {
    // https://d3js.org/d3-time-format/ Version 2.1.0. Copyright 2017 Mike Bostock.
    (function (global, factory) {
      (typeof exports === "undefined" ? "undefined" : _typeof(exports)) === 'object' && typeof module !== 'undefined' ? factory(exports, require('d3-time')) : typeof define === 'function' && define.amd ? define(['exports', 'd3-time'], factory) : factory(global.d3 = global.d3 || {}, global.d3);
    })(this, function (exports, d3Time) {
      'use strict';

      function localDate(d) {
        if (0 <= d.y && d.y < 100) {
          var date = new Date(-1, d.m, d.d, d.H, d.M, d.S, d.L);
          date.setFullYear(d.y);
          return date;
        }
        return new Date(d.y, d.m, d.d, d.H, d.M, d.S, d.L);
      }

      function utcDate(d) {
        if (0 <= d.y && d.y < 100) {
          var date = new Date(Date.UTC(-1, d.m, d.d, d.H, d.M, d.S, d.L));
          date.setUTCFullYear(d.y);
          return date;
        }
        return new Date(Date.UTC(d.y, d.m, d.d, d.H, d.M, d.S, d.L));
      }

      function newYear(y) {
        return { y: y, m: 0, d: 1, H: 0, M: 0, S: 0, L: 0 };
      }

      function formatLocale(locale) {
        var locale_dateTime = locale.dateTime,
            locale_date = locale.date,
            locale_time = locale.time,
            locale_periods = locale.periods,
            locale_weekdays = locale.days,
            locale_shortWeekdays = locale.shortDays,
            locale_months = locale.months,
            locale_shortMonths = locale.shortMonths;

        var periodRe = formatRe(locale_periods),
            periodLookup = formatLookup(locale_periods),
            weekdayRe = formatRe(locale_weekdays),
            weekdayLookup = formatLookup(locale_weekdays),
            shortWeekdayRe = formatRe(locale_shortWeekdays),
            shortWeekdayLookup = formatLookup(locale_shortWeekdays),
            monthRe = formatRe(locale_months),
            monthLookup = formatLookup(locale_months),
            shortMonthRe = formatRe(locale_shortMonths),
            shortMonthLookup = formatLookup(locale_shortMonths);

        var formats = {
          "a": formatShortWeekday,
          "A": formatWeekday,
          "b": formatShortMonth,
          "B": formatMonth,
          "c": null,
          "d": formatDayOfMonth,
          "e": formatDayOfMonth,
          "f": formatMicroseconds,
          "H": formatHour24,
          "I": formatHour12,
          "j": formatDayOfYear,
          "L": formatMilliseconds,
          "m": formatMonthNumber,
          "M": formatMinutes,
          "p": formatPeriod,
          "Q": formatUnixTimestamp,
          "s": formatUnixTimestampSeconds,
          "S": formatSeconds,
          "u": formatWeekdayNumberMonday,
          "U": formatWeekNumberSunday,
          "V": formatWeekNumberISO,
          "w": formatWeekdayNumberSunday,
          "W": formatWeekNumberMonday,
          "x": null,
          "X": null,
          "y": formatYear,
          "Y": formatFullYear,
          "Z": formatZone,
          "%": formatLiteralPercent
        };

        var utcFormats = {
          "a": formatUTCShortWeekday,
          "A": formatUTCWeekday,
          "b": formatUTCShortMonth,
          "B": formatUTCMonth,
          "c": null,
          "d": formatUTCDayOfMonth,
          "e": formatUTCDayOfMonth,
          "f": formatUTCMicroseconds,
          "H": formatUTCHour24,
          "I": formatUTCHour12,
          "j": formatUTCDayOfYear,
          "L": formatUTCMilliseconds,
          "m": formatUTCMonthNumber,
          "M": formatUTCMinutes,
          "p": formatUTCPeriod,
          "Q": formatUnixTimestamp,
          "s": formatUnixTimestampSeconds,
          "S": formatUTCSeconds,
          "u": formatUTCWeekdayNumberMonday,
          "U": formatUTCWeekNumberSunday,
          "V": formatUTCWeekNumberISO,
          "w": formatUTCWeekdayNumberSunday,
          "W": formatUTCWeekNumberMonday,
          "x": null,
          "X": null,
          "y": formatUTCYear,
          "Y": formatUTCFullYear,
          "Z": formatUTCZone,
          "%": formatLiteralPercent
        };

        var parses = {
          "a": parseShortWeekday,
          "A": parseWeekday,
          "b": parseShortMonth,
          "B": parseMonth,
          "c": parseLocaleDateTime,
          "d": parseDayOfMonth,
          "e": parseDayOfMonth,
          "f": parseMicroseconds,
          "H": parseHour24,
          "I": parseHour24,
          "j": parseDayOfYear,
          "L": parseMilliseconds,
          "m": parseMonthNumber,
          "M": parseMinutes,
          "p": parsePeriod,
          "Q": parseUnixTimestamp,
          "s": parseUnixTimestampSeconds,
          "S": parseSeconds,
          "u": parseWeekdayNumberMonday,
          "U": parseWeekNumberSunday,
          "V": parseWeekNumberISO,
          "w": parseWeekdayNumberSunday,
          "W": parseWeekNumberMonday,
          "x": parseLocaleDate,
          "X": parseLocaleTime,
          "y": parseYear,
          "Y": parseFullYear,
          "Z": parseZone,
          "%": parseLiteralPercent
        };

        // These recursive directive definitions must be deferred.
        formats.x = newFormat(locale_date, formats);
        formats.X = newFormat(locale_time, formats);
        formats.c = newFormat(locale_dateTime, formats);
        utcFormats.x = newFormat(locale_date, utcFormats);
        utcFormats.X = newFormat(locale_time, utcFormats);
        utcFormats.c = newFormat(locale_dateTime, utcFormats);

        function newFormat(specifier, formats) {
          return function (date) {
            var string = [],
                i = -1,
                j = 0,
                n = specifier.length,
                c,
                pad,
                format;

            if (!(date instanceof Date)) date = new Date(+date);

            while (++i < n) {
              if (specifier.charCodeAt(i) === 37) {
                string.push(specifier.slice(j, i));
                if ((pad = pads[c = specifier.charAt(++i)]) != null) c = specifier.charAt(++i);else pad = c === "e" ? " " : "0";
                if (format = formats[c]) c = format(date, pad);
                string.push(c);
                j = i + 1;
              }
            }

            string.push(specifier.slice(j, i));
            return string.join("");
          };
        }

        function newParse(specifier, newDate) {
          return function (string) {
            var d = newYear(1900),
                i = parseSpecifier(d, specifier, string += "", 0),
                week,
                day;
            if (i != string.length) return null;

            // If a UNIX timestamp is specified, return it.
            if ("Q" in d) return new Date(d.Q);

            // The am-pm flag is 0 for AM, and 1 for PM.
            if ("p" in d) d.H = d.H % 12 + d.p * 12;

            // Convert day-of-week and week-of-year to day-of-year.
            if ("V" in d) {
              if (d.V < 1 || d.V > 53) return null;
              if (!("w" in d)) d.w = 1;
              if ("Z" in d) {
                week = utcDate(newYear(d.y)), day = week.getUTCDay();
                week = day > 4 || day === 0 ? d3Time.utcMonday.ceil(week) : d3Time.utcMonday(week);
                week = d3Time.utcDay.offset(week, (d.V - 1) * 7);
                d.y = week.getUTCFullYear();
                d.m = week.getUTCMonth();
                d.d = week.getUTCDate() + (d.w + 6) % 7;
              } else {
                week = newDate(newYear(d.y)), day = week.getDay();
                week = day > 4 || day === 0 ? d3Time.timeMonday.ceil(week) : d3Time.timeMonday(week);
                week = d3Time.timeDay.offset(week, (d.V - 1) * 7);
                d.y = week.getFullYear();
                d.m = week.getMonth();
                d.d = week.getDate() + (d.w + 6) % 7;
              }
            } else if ("W" in d || "U" in d) {
              if (!("w" in d)) d.w = "u" in d ? d.u % 7 : "W" in d ? 1 : 0;
              day = "Z" in d ? utcDate(newYear(d.y)).getUTCDay() : newDate(newYear(d.y)).getDay();
              d.m = 0;
              d.d = "W" in d ? (d.w + 6) % 7 + d.W * 7 - (day + 5) % 7 : d.w + d.U * 7 - (day + 6) % 7;
            }

            // If a time zone is specified, all fields are interpreted as UTC and then
            // offset according to the specified time zone.
            if ("Z" in d) {
              d.H += d.Z / 100 | 0;
              d.M += d.Z % 100;
              return utcDate(d);
            }

            // Otherwise, all fields are in local time.
            return newDate(d);
          };
        }

        function parseSpecifier(d, specifier, string, j) {
          var i = 0,
              n = specifier.length,
              m = string.length,
              c,
              parse;

          while (i < n) {
            if (j >= m) return -1;
            c = specifier.charCodeAt(i++);
            if (c === 37) {
              c = specifier.charAt(i++);
              parse = parses[c in pads ? specifier.charAt(i++) : c];
              if (!parse || (j = parse(d, string, j)) < 0) return -1;
            } else if (c != string.charCodeAt(j++)) {
              return -1;
            }
          }

          return j;
        }

        function parsePeriod(d, string, i) {
          var n = periodRe.exec(string.slice(i));
          return n ? (d.p = periodLookup[n[0].toLowerCase()], i + n[0].length) : -1;
        }

        function parseShortWeekday(d, string, i) {
          var n = shortWeekdayRe.exec(string.slice(i));
          return n ? (d.w = shortWeekdayLookup[n[0].toLowerCase()], i + n[0].length) : -1;
        }

        function parseWeekday(d, string, i) {
          var n = weekdayRe.exec(string.slice(i));
          return n ? (d.w = weekdayLookup[n[0].toLowerCase()], i + n[0].length) : -1;
        }

        function parseShortMonth(d, string, i) {
          var n = shortMonthRe.exec(string.slice(i));
          return n ? (d.m = shortMonthLookup[n[0].toLowerCase()], i + n[0].length) : -1;
        }

        function parseMonth(d, string, i) {
          var n = monthRe.exec(string.slice(i));
          return n ? (d.m = monthLookup[n[0].toLowerCase()], i + n[0].length) : -1;
        }

        function parseLocaleDateTime(d, string, i) {
          return parseSpecifier(d, locale_dateTime, string, i);
        }

        function parseLocaleDate(d, string, i) {
          return parseSpecifier(d, locale_date, string, i);
        }

        function parseLocaleTime(d, string, i) {
          return parseSpecifier(d, locale_time, string, i);
        }

        function formatShortWeekday(d) {
          return locale_shortWeekdays[d.getDay()];
        }

        function formatWeekday(d) {
          return locale_weekdays[d.getDay()];
        }

        function formatShortMonth(d) {
          return locale_shortMonths[d.getMonth()];
        }

        function formatMonth(d) {
          return locale_months[d.getMonth()];
        }

        function formatPeriod(d) {
          return locale_periods[+(d.getHours() >= 12)];
        }

        function formatUTCShortWeekday(d) {
          return locale_shortWeekdays[d.getUTCDay()];
        }

        function formatUTCWeekday(d) {
          return locale_weekdays[d.getUTCDay()];
        }

        function formatUTCShortMonth(d) {
          return locale_shortMonths[d.getUTCMonth()];
        }

        function formatUTCMonth(d) {
          return locale_months[d.getUTCMonth()];
        }

        function formatUTCPeriod(d) {
          return locale_periods[+(d.getUTCHours() >= 12)];
        }

        return {
          format: function format(specifier) {
            var f = newFormat(specifier += "", formats);
            f.toString = function () {
              return specifier;
            };
            return f;
          },
          parse: function parse(specifier) {
            var p = newParse(specifier += "", localDate);
            p.toString = function () {
              return specifier;
            };
            return p;
          },
          utcFormat: function utcFormat(specifier) {
            var f = newFormat(specifier += "", utcFormats);
            f.toString = function () {
              return specifier;
            };
            return f;
          },
          utcParse: function utcParse(specifier) {
            var p = newParse(specifier, utcDate);
            p.toString = function () {
              return specifier;
            };
            return p;
          }
        };
      }

      var pads = { "-": "", "_": " ", "0": "0" };
      var numberRe = /^\s*\d+/;
      var percentRe = /^%/;
      var requoteRe = /[\\^$*+?|[\]().{}]/g;

      function pad(value, fill, width) {
        var sign = value < 0 ? "-" : "",
            string = (sign ? -value : value) + "",
            length = string.length;
        return sign + (length < width ? new Array(width - length + 1).join(fill) + string : string);
      }

      function requote(s) {
        return s.replace(requoteRe, "\\$&");
      }

      function formatRe(names) {
        return new RegExp("^(?:" + names.map(requote).join("|") + ")", "i");
      }

      function formatLookup(names) {
        var map = {},
            i = -1,
            n = names.length;
        while (++i < n) {
          map[names[i].toLowerCase()] = i;
        }return map;
      }

      function parseWeekdayNumberSunday(d, string, i) {
        var n = numberRe.exec(string.slice(i, i + 1));
        return n ? (d.w = +n[0], i + n[0].length) : -1;
      }

      function parseWeekdayNumberMonday(d, string, i) {
        var n = numberRe.exec(string.slice(i, i + 1));
        return n ? (d.u = +n[0], i + n[0].length) : -1;
      }

      function parseWeekNumberSunday(d, string, i) {
        var n = numberRe.exec(string.slice(i, i + 2));
        return n ? (d.U = +n[0], i + n[0].length) : -1;
      }

      function parseWeekNumberISO(d, string, i) {
        var n = numberRe.exec(string.slice(i, i + 2));
        return n ? (d.V = +n[0], i + n[0].length) : -1;
      }

      function parseWeekNumberMonday(d, string, i) {
        var n = numberRe.exec(string.slice(i, i + 2));
        return n ? (d.W = +n[0], i + n[0].length) : -1;
      }

      function parseFullYear(d, string, i) {
        var n = numberRe.exec(string.slice(i, i + 4));
        return n ? (d.y = +n[0], i + n[0].length) : -1;
      }

      function parseYear(d, string, i) {
        var n = numberRe.exec(string.slice(i, i + 2));
        return n ? (d.y = +n[0] + (+n[0] > 68 ? 1900 : 2000), i + n[0].length) : -1;
      }

      function parseZone(d, string, i) {
        var n = /^(Z)|([+-]\d\d)(?::?(\d\d))?/.exec(string.slice(i, i + 6));
        return n ? (d.Z = n[1] ? 0 : -(n[2] + (n[3] || "00")), i + n[0].length) : -1;
      }

      function parseMonthNumber(d, string, i) {
        var n = numberRe.exec(string.slice(i, i + 2));
        return n ? (d.m = n[0] - 1, i + n[0].length) : -1;
      }

      function parseDayOfMonth(d, string, i) {
        var n = numberRe.exec(string.slice(i, i + 2));
        return n ? (d.d = +n[0], i + n[0].length) : -1;
      }

      function parseDayOfYear(d, string, i) {
        var n = numberRe.exec(string.slice(i, i + 3));
        return n ? (d.m = 0, d.d = +n[0], i + n[0].length) : -1;
      }

      function parseHour24(d, string, i) {
        var n = numberRe.exec(string.slice(i, i + 2));
        return n ? (d.H = +n[0], i + n[0].length) : -1;
      }

      function parseMinutes(d, string, i) {
        var n = numberRe.exec(string.slice(i, i + 2));
        return n ? (d.M = +n[0], i + n[0].length) : -1;
      }

      function parseSeconds(d, string, i) {
        var n = numberRe.exec(string.slice(i, i + 2));
        return n ? (d.S = +n[0], i + n[0].length) : -1;
      }

      function parseMilliseconds(d, string, i) {
        var n = numberRe.exec(string.slice(i, i + 3));
        return n ? (d.L = +n[0], i + n[0].length) : -1;
      }

      function parseMicroseconds(d, string, i) {
        var n = numberRe.exec(string.slice(i, i + 6));
        return n ? (d.L = Math.floor(n[0] / 1000), i + n[0].length) : -1;
      }

      function parseLiteralPercent(d, string, i) {
        var n = percentRe.exec(string.slice(i, i + 1));
        return n ? i + n[0].length : -1;
      }

      function parseUnixTimestamp(d, string, i) {
        var n = numberRe.exec(string.slice(i));
        return n ? (d.Q = +n[0], i + n[0].length) : -1;
      }

      function parseUnixTimestampSeconds(d, string, i) {
        var n = numberRe.exec(string.slice(i));
        return n ? (d.Q = +n[0] * 1000, i + n[0].length) : -1;
      }

      function formatDayOfMonth(d, p) {
        return pad(d.getDate(), p, 2);
      }

      function formatHour24(d, p) {
        return pad(d.getHours(), p, 2);
      }

      function formatHour12(d, p) {
        return pad(d.getHours() % 12 || 12, p, 2);
      }

      function formatDayOfYear(d, p) {
        return pad(1 + d3Time.timeDay.count(d3Time.timeYear(d), d), p, 3);
      }

      function formatMilliseconds(d, p) {
        return pad(d.getMilliseconds(), p, 3);
      }

      function formatMicroseconds(d, p) {
        return formatMilliseconds(d, p) + "000";
      }

      function formatMonthNumber(d, p) {
        return pad(d.getMonth() + 1, p, 2);
      }

      function formatMinutes(d, p) {
        return pad(d.getMinutes(), p, 2);
      }

      function formatSeconds(d, p) {
        return pad(d.getSeconds(), p, 2);
      }

      function formatWeekdayNumberMonday(d) {
        var day = d.getDay();
        return day === 0 ? 7 : day;
      }

      function formatWeekNumberSunday(d, p) {
        return pad(d3Time.timeSunday.count(d3Time.timeYear(d), d), p, 2);
      }

      function formatWeekNumberISO(d, p) {
        var day = d.getDay();
        d = day >= 4 || day === 0 ? d3Time.timeThursday(d) : d3Time.timeThursday.ceil(d);
        return pad(d3Time.timeThursday.count(d3Time.timeYear(d), d) + (d3Time.timeYear(d).getDay() === 4), p, 2);
      }

      function formatWeekdayNumberSunday(d) {
        return d.getDay();
      }

      function formatWeekNumberMonday(d, p) {
        return pad(d3Time.timeMonday.count(d3Time.timeYear(d), d), p, 2);
      }

      function formatYear(d, p) {
        return pad(d.getFullYear() % 100, p, 2);
      }

      function formatFullYear(d, p) {
        return pad(d.getFullYear() % 10000, p, 4);
      }

      function formatZone(d) {
        var z = d.getTimezoneOffset();
        return (z > 0 ? "-" : (z *= -1, "+")) + pad(z / 60 | 0, "0", 2) + pad(z % 60, "0", 2);
      }

      function formatUTCDayOfMonth(d, p) {
        return pad(d.getUTCDate(), p, 2);
      }

      function formatUTCHour24(d, p) {
        return pad(d.getUTCHours(), p, 2);
      }

      function formatUTCHour12(d, p) {
        return pad(d.getUTCHours() % 12 || 12, p, 2);
      }

      function formatUTCDayOfYear(d, p) {
        return pad(1 + d3Time.utcDay.count(d3Time.utcYear(d), d), p, 3);
      }

      function formatUTCMilliseconds(d, p) {
        return pad(d.getUTCMilliseconds(), p, 3);
      }

      function formatUTCMicroseconds(d, p) {
        return formatUTCMilliseconds(d, p) + "000";
      }

      function formatUTCMonthNumber(d, p) {
        return pad(d.getUTCMonth() + 1, p, 2);
      }

      function formatUTCMinutes(d, p) {
        return pad(d.getUTCMinutes(), p, 2);
      }

      function formatUTCSeconds(d, p) {
        return pad(d.getUTCSeconds(), p, 2);
      }

      function formatUTCWeekdayNumberMonday(d) {
        var dow = d.getUTCDay();
        return dow === 0 ? 7 : dow;
      }

      function formatUTCWeekNumberSunday(d, p) {
        return pad(d3Time.utcSunday.count(d3Time.utcYear(d), d), p, 2);
      }

      function formatUTCWeekNumberISO(d, p) {
        var day = d.getUTCDay();
        d = day >= 4 || day === 0 ? d3Time.utcThursday(d) : d3Time.utcThursday.ceil(d);
        return pad(d3Time.utcThursday.count(d3Time.utcYear(d), d) + (d3Time.utcYear(d).getUTCDay() === 4), p, 2);
      }

      function formatUTCWeekdayNumberSunday(d) {
        return d.getUTCDay();
      }

      function formatUTCWeekNumberMonday(d, p) {
        return pad(d3Time.utcMonday.count(d3Time.utcYear(d), d), p, 2);
      }

      function formatUTCYear(d, p) {
        return pad(d.getUTCFullYear() % 100, p, 2);
      }

      function formatUTCFullYear(d, p) {
        return pad(d.getUTCFullYear() % 10000, p, 4);
      }

      function formatUTCZone() {
        return "+0000";
      }

      function formatLiteralPercent() {
        return "%";
      }

      function formatUnixTimestamp(d) {
        return +d;
      }

      function formatUnixTimestampSeconds(d) {
        return Math.floor(+d / 1000);
      }

      var locale;

      defaultLocale({
        dateTime: "%x, %X",
        date: "%-m/%-d/%Y",
        time: "%-I:%M:%S %p",
        periods: ["AM", "PM"],
        days: ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"],
        shortDays: ["Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"],
        months: ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"],
        shortMonths: ["Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"]
      });

      function defaultLocale(definition) {
        locale = formatLocale(definition);
        exports.timeFormat = locale.format;
        exports.timeParse = locale.parse;
        exports.utcFormat = locale.utcFormat;
        exports.utcParse = locale.utcParse;
        return locale;
      }

      var isoSpecifier = "%Y-%m-%dT%H:%M:%S.%LZ";

      function formatIsoNative(date) {
        return date.toISOString();
      }

      var formatIso = Date.prototype.toISOString ? formatIsoNative : exports.utcFormat(isoSpecifier);

      function parseIsoNative(string) {
        var date = new Date(string);
        return isNaN(date) ? null : date;
      }

      var parseIso = +new Date("2000-01-01T00:00:00.000Z") ? parseIsoNative : exports.utcParse(isoSpecifier);

      exports.timeFormatDefaultLocale = defaultLocale;
      exports.timeFormatLocale = formatLocale;
      exports.isoFormat = formatIso;
      exports.isoParse = parseIso;

      Object.defineProperty(exports, '__esModule', { value: true });
    });
  }, { "d3-time": 9 }], 9: [function (require, module, exports) {
    // https://d3js.org/d3-time/ Version 1.0.7. Copyright 2017 Mike Bostock.
    (function (global, factory) {
      (typeof exports === "undefined" ? "undefined" : _typeof(exports)) === 'object' && typeof module !== 'undefined' ? factory(exports) : typeof define === 'function' && define.amd ? define(['exports'], factory) : factory(global.d3 = global.d3 || {});
    })(this, function (exports) {
      'use strict';

      var t0 = new Date();
      var t1 = new Date();

      function newInterval(floori, offseti, count, field) {

        function interval(date) {
          return floori(date = new Date(+date)), date;
        }

        interval.floor = interval;

        interval.ceil = function (date) {
          return floori(date = new Date(date - 1)), offseti(date, 1), floori(date), date;
        };

        interval.round = function (date) {
          var d0 = interval(date),
              d1 = interval.ceil(date);
          return date - d0 < d1 - date ? d0 : d1;
        };

        interval.offset = function (date, step) {
          return offseti(date = new Date(+date), step == null ? 1 : Math.floor(step)), date;
        };

        interval.range = function (start, stop, step) {
          var range = [];
          start = interval.ceil(start);
          step = step == null ? 1 : Math.floor(step);
          if (!(start < stop) || !(step > 0)) return range; // also handles Invalid Date
          do {
            range.push(new Date(+start));
          } while ((offseti(start, step), floori(start), start < stop));
          return range;
        };

        interval.filter = function (test) {
          return newInterval(function (date) {
            if (date >= date) while (floori(date), !test(date)) {
              date.setTime(date - 1);
            }
          }, function (date, step) {
            if (date >= date) {
              if (step < 0) while (++step <= 0) {
                while (offseti(date, -1), !test(date)) {} // eslint-disable-line no-empty
              } else while (--step >= 0) {
                while (offseti(date, +1), !test(date)) {} // eslint-disable-line no-empty
              }
            }
          });
        };

        if (count) {
          interval.count = function (start, end) {
            t0.setTime(+start), t1.setTime(+end);
            floori(t0), floori(t1);
            return Math.floor(count(t0, t1));
          };

          interval.every = function (step) {
            step = Math.floor(step);
            return !isFinite(step) || !(step > 0) ? null : !(step > 1) ? interval : interval.filter(field ? function (d) {
              return field(d) % step === 0;
            } : function (d) {
              return interval.count(0, d) % step === 0;
            });
          };
        }

        return interval;
      }

      var millisecond = newInterval(function () {
        // noop
      }, function (date, step) {
        date.setTime(+date + step);
      }, function (start, end) {
        return end - start;
      });

      // An optimized implementation for this simple case.
      millisecond.every = function (k) {
        k = Math.floor(k);
        if (!isFinite(k) || !(k > 0)) return null;
        if (!(k > 1)) return millisecond;
        return newInterval(function (date) {
          date.setTime(Math.floor(date / k) * k);
        }, function (date, step) {
          date.setTime(+date + step * k);
        }, function (start, end) {
          return (end - start) / k;
        });
      };

      var milliseconds = millisecond.range;

      var durationSecond = 1e3;
      var durationMinute = 6e4;
      var durationHour = 36e5;
      var durationDay = 864e5;
      var durationWeek = 6048e5;

      var second = newInterval(function (date) {
        date.setTime(Math.floor(date / durationSecond) * durationSecond);
      }, function (date, step) {
        date.setTime(+date + step * durationSecond);
      }, function (start, end) {
        return (end - start) / durationSecond;
      }, function (date) {
        return date.getUTCSeconds();
      });

      var seconds = second.range;

      var minute = newInterval(function (date) {
        date.setTime(Math.floor(date / durationMinute) * durationMinute);
      }, function (date, step) {
        date.setTime(+date + step * durationMinute);
      }, function (start, end) {
        return (end - start) / durationMinute;
      }, function (date) {
        return date.getMinutes();
      });

      var minutes = minute.range;

      var hour = newInterval(function (date) {
        var offset = date.getTimezoneOffset() * durationMinute % durationHour;
        if (offset < 0) offset += durationHour;
        date.setTime(Math.floor((+date - offset) / durationHour) * durationHour + offset);
      }, function (date, step) {
        date.setTime(+date + step * durationHour);
      }, function (start, end) {
        return (end - start) / durationHour;
      }, function (date) {
        return date.getHours();
      });

      var hours = hour.range;

      var day = newInterval(function (date) {
        date.setHours(0, 0, 0, 0);
      }, function (date, step) {
        date.setDate(date.getDate() + step);
      }, function (start, end) {
        return (end - start - (end.getTimezoneOffset() - start.getTimezoneOffset()) * durationMinute) / durationDay;
      }, function (date) {
        return date.getDate() - 1;
      });

      var days = day.range;

      function weekday(i) {
        return newInterval(function (date) {
          date.setDate(date.getDate() - (date.getDay() + 7 - i) % 7);
          date.setHours(0, 0, 0, 0);
        }, function (date, step) {
          date.setDate(date.getDate() + step * 7);
        }, function (start, end) {
          return (end - start - (end.getTimezoneOffset() - start.getTimezoneOffset()) * durationMinute) / durationWeek;
        });
      }

      var sunday = weekday(0);
      var monday = weekday(1);
      var tuesday = weekday(2);
      var wednesday = weekday(3);
      var thursday = weekday(4);
      var friday = weekday(5);
      var saturday = weekday(6);

      var sundays = sunday.range;
      var mondays = monday.range;
      var tuesdays = tuesday.range;
      var wednesdays = wednesday.range;
      var thursdays = thursday.range;
      var fridays = friday.range;
      var saturdays = saturday.range;

      var month = newInterval(function (date) {
        date.setDate(1);
        date.setHours(0, 0, 0, 0);
      }, function (date, step) {
        date.setMonth(date.getMonth() + step);
      }, function (start, end) {
        return end.getMonth() - start.getMonth() + (end.getFullYear() - start.getFullYear()) * 12;
      }, function (date) {
        return date.getMonth();
      });

      var months = month.range;

      var year = newInterval(function (date) {
        date.setMonth(0, 1);
        date.setHours(0, 0, 0, 0);
      }, function (date, step) {
        date.setFullYear(date.getFullYear() + step);
      }, function (start, end) {
        return end.getFullYear() - start.getFullYear();
      }, function (date) {
        return date.getFullYear();
      });

      // An optimized implementation for this simple case.
      year.every = function (k) {
        return !isFinite(k = Math.floor(k)) || !(k > 0) ? null : newInterval(function (date) {
          date.setFullYear(Math.floor(date.getFullYear() / k) * k);
          date.setMonth(0, 1);
          date.setHours(0, 0, 0, 0);
        }, function (date, step) {
          date.setFullYear(date.getFullYear() + step * k);
        });
      };

      var years = year.range;

      var utcMinute = newInterval(function (date) {
        date.setUTCSeconds(0, 0);
      }, function (date, step) {
        date.setTime(+date + step * durationMinute);
      }, function (start, end) {
        return (end - start) / durationMinute;
      }, function (date) {
        return date.getUTCMinutes();
      });

      var utcMinutes = utcMinute.range;

      var utcHour = newInterval(function (date) {
        date.setUTCMinutes(0, 0, 0);
      }, function (date, step) {
        date.setTime(+date + step * durationHour);
      }, function (start, end) {
        return (end - start) / durationHour;
      }, function (date) {
        return date.getUTCHours();
      });

      var utcHours = utcHour.range;

      var utcDay = newInterval(function (date) {
        date.setUTCHours(0, 0, 0, 0);
      }, function (date, step) {
        date.setUTCDate(date.getUTCDate() + step);
      }, function (start, end) {
        return (end - start) / durationDay;
      }, function (date) {
        return date.getUTCDate() - 1;
      });

      var utcDays = utcDay.range;

      function utcWeekday(i) {
        return newInterval(function (date) {
          date.setUTCDate(date.getUTCDate() - (date.getUTCDay() + 7 - i) % 7);
          date.setUTCHours(0, 0, 0, 0);
        }, function (date, step) {
          date.setUTCDate(date.getUTCDate() + step * 7);
        }, function (start, end) {
          return (end - start) / durationWeek;
        });
      }

      var utcSunday = utcWeekday(0);
      var utcMonday = utcWeekday(1);
      var utcTuesday = utcWeekday(2);
      var utcWednesday = utcWeekday(3);
      var utcThursday = utcWeekday(4);
      var utcFriday = utcWeekday(5);
      var utcSaturday = utcWeekday(6);

      var utcSundays = utcSunday.range;
      var utcMondays = utcMonday.range;
      var utcTuesdays = utcTuesday.range;
      var utcWednesdays = utcWednesday.range;
      var utcThursdays = utcThursday.range;
      var utcFridays = utcFriday.range;
      var utcSaturdays = utcSaturday.range;

      var utcMonth = newInterval(function (date) {
        date.setUTCDate(1);
        date.setUTCHours(0, 0, 0, 0);
      }, function (date, step) {
        date.setUTCMonth(date.getUTCMonth() + step);
      }, function (start, end) {
        return end.getUTCMonth() - start.getUTCMonth() + (end.getUTCFullYear() - start.getUTCFullYear()) * 12;
      }, function (date) {
        return date.getUTCMonth();
      });

      var utcMonths = utcMonth.range;

      var utcYear = newInterval(function (date) {
        date.setUTCMonth(0, 1);
        date.setUTCHours(0, 0, 0, 0);
      }, function (date, step) {
        date.setUTCFullYear(date.getUTCFullYear() + step);
      }, function (start, end) {
        return end.getUTCFullYear() - start.getUTCFullYear();
      }, function (date) {
        return date.getUTCFullYear();
      });

      // An optimized implementation for this simple case.
      utcYear.every = function (k) {
        return !isFinite(k = Math.floor(k)) || !(k > 0) ? null : newInterval(function (date) {
          date.setUTCFullYear(Math.floor(date.getUTCFullYear() / k) * k);
          date.setUTCMonth(0, 1);
          date.setUTCHours(0, 0, 0, 0);
        }, function (date, step) {
          date.setUTCFullYear(date.getUTCFullYear() + step * k);
        });
      };

      var utcYears = utcYear.range;

      exports.timeInterval = newInterval;
      exports.timeMillisecond = millisecond;
      exports.timeMilliseconds = milliseconds;
      exports.utcMillisecond = millisecond;
      exports.utcMilliseconds = milliseconds;
      exports.timeSecond = second;
      exports.timeSeconds = seconds;
      exports.utcSecond = second;
      exports.utcSeconds = seconds;
      exports.timeMinute = minute;
      exports.timeMinutes = minutes;
      exports.timeHour = hour;
      exports.timeHours = hours;
      exports.timeDay = day;
      exports.timeDays = days;
      exports.timeWeek = sunday;
      exports.timeWeeks = sundays;
      exports.timeSunday = sunday;
      exports.timeSundays = sundays;
      exports.timeMonday = monday;
      exports.timeMondays = mondays;
      exports.timeTuesday = tuesday;
      exports.timeTuesdays = tuesdays;
      exports.timeWednesday = wednesday;
      exports.timeWednesdays = wednesdays;
      exports.timeThursday = thursday;
      exports.timeThursdays = thursdays;
      exports.timeFriday = friday;
      exports.timeFridays = fridays;
      exports.timeSaturday = saturday;
      exports.timeSaturdays = saturdays;
      exports.timeMonth = month;
      exports.timeMonths = months;
      exports.timeYear = year;
      exports.timeYears = years;
      exports.utcMinute = utcMinute;
      exports.utcMinutes = utcMinutes;
      exports.utcHour = utcHour;
      exports.utcHours = utcHours;
      exports.utcDay = utcDay;
      exports.utcDays = utcDays;
      exports.utcWeek = utcSunday;
      exports.utcWeeks = utcSundays;
      exports.utcSunday = utcSunday;
      exports.utcSundays = utcSundays;
      exports.utcMonday = utcMonday;
      exports.utcMondays = utcMondays;
      exports.utcTuesday = utcTuesday;
      exports.utcTuesdays = utcTuesdays;
      exports.utcWednesday = utcWednesday;
      exports.utcWednesdays = utcWednesdays;
      exports.utcThursday = utcThursday;
      exports.utcThursdays = utcThursdays;
      exports.utcFriday = utcFriday;
      exports.utcFridays = utcFridays;
      exports.utcSaturday = utcSaturday;
      exports.utcSaturdays = utcSaturdays;
      exports.utcMonth = utcMonth;
      exports.utcMonths = utcMonths;
      exports.utcYear = utcYear;
      exports.utcYears = utcYears;

      Object.defineProperty(exports, '__esModule', { value: true });
    });
  }, {}], 10: [function (require, module, exports) {
    /**
     * lodash (Custom Build) <https://lodash.com/>
     * Build: `lodash modularize exports="npm" -o ./`
     * Copyright jQuery Foundation and other contributors <https://jquery.org/>
     * Released under MIT license <https://lodash.com/license>
     * Based on Underscore.js 1.8.3 <http://underscorejs.org/LICENSE>
     * Copyright Jeremy Ashkenas, DocumentCloud and Investigative Reporters & Editors
     */

    /** Used as references for various `Number` constants. */
    var MAX_SAFE_INTEGER = 9007199254740991;

    /** `Object#toString` result references. */
    var argsTag = '[object Arguments]',
        funcTag = '[object Function]',
        genTag = '[object GeneratorFunction]';

    /** Used to detect unsigned integer values. */
    var reIsUint = /^(?:0|[1-9]\d*)$/;

    /**
     * A faster alternative to `Function#apply`, this function invokes `func`
     * with the `this` binding of `thisArg` and the arguments of `args`.
     *
     * @private
     * @param {Function} func The function to invoke.
     * @param {*} thisArg The `this` binding of `func`.
     * @param {Array} args The arguments to invoke `func` with.
     * @returns {*} Returns the result of `func`.
     */
    function apply(func, thisArg, args) {
      switch (args.length) {
        case 0:
          return func.call(thisArg);
        case 1:
          return func.call(thisArg, args[0]);
        case 2:
          return func.call(thisArg, args[0], args[1]);
        case 3:
          return func.call(thisArg, args[0], args[1], args[2]);
      }
      return func.apply(thisArg, args);
    }

    /**
     * The base implementation of `_.times` without support for iteratee shorthands
     * or max array length checks.
     *
     * @private
     * @param {number} n The number of times to invoke `iteratee`.
     * @param {Function} iteratee The function invoked per iteration.
     * @returns {Array} Returns the array of results.
     */
    function baseTimes(n, iteratee) {
      var index = -1,
          result = Array(n);

      while (++index < n) {
        result[index] = iteratee(index);
      }
      return result;
    }

    /**
     * Creates a unary function that invokes `func` with its argument transformed.
     *
     * @private
     * @param {Function} func The function to wrap.
     * @param {Function} transform The argument transform.
     * @returns {Function} Returns the new function.
     */
    function overArg(func, transform) {
      return function (arg) {
        return func(transform(arg));
      };
    }

    /** Used for built-in method references. */
    var objectProto = Object.prototype;

    /** Used to check objects for own properties. */
    var hasOwnProperty = objectProto.hasOwnProperty;

    /**
     * Used to resolve the
     * [`toStringTag`](http://ecma-international.org/ecma-262/7.0/#sec-object.prototype.tostring)
     * of values.
     */
    var objectToString = objectProto.toString;

    /** Built-in value references. */
    var propertyIsEnumerable = objectProto.propertyIsEnumerable;

    /* Built-in method references for those with the same name as other `lodash` methods. */
    var nativeKeys = overArg(Object.keys, Object),
        nativeMax = Math.max;

    /** Detect if properties shadowing those on `Object.prototype` are non-enumerable. */
    var nonEnumShadows = !propertyIsEnumerable.call({ 'valueOf': 1 }, 'valueOf');

    /**
     * Creates an array of the enumerable property names of the array-like `value`.
     *
     * @private
     * @param {*} value The value to query.
     * @param {boolean} inherited Specify returning inherited property names.
     * @returns {Array} Returns the array of property names.
     */
    function arrayLikeKeys(value, inherited) {
      // Safari 8.1 makes `arguments.callee` enumerable in strict mode.
      // Safari 9 makes `arguments.length` enumerable in strict mode.
      var result = isArray(value) || isArguments(value) ? baseTimes(value.length, String) : [];

      var length = result.length,
          skipIndexes = !!length;

      for (var key in value) {
        if ((inherited || hasOwnProperty.call(value, key)) && !(skipIndexes && (key == 'length' || isIndex(key, length)))) {
          result.push(key);
        }
      }
      return result;
    }

    /**
     * Assigns `value` to `key` of `object` if the existing value is not equivalent
     * using [`SameValueZero`](http://ecma-international.org/ecma-262/7.0/#sec-samevaluezero)
     * for equality comparisons.
     *
     * @private
     * @param {Object} object The object to modify.
     * @param {string} key The key of the property to assign.
     * @param {*} value The value to assign.
     */
    function assignValue(object, key, value) {
      var objValue = object[key];
      if (!(hasOwnProperty.call(object, key) && eq(objValue, value)) || value === undefined && !(key in object)) {
        object[key] = value;
      }
    }

    /**
     * The base implementation of `_.keys` which doesn't treat sparse arrays as dense.
     *
     * @private
     * @param {Object} object The object to query.
     * @returns {Array} Returns the array of property names.
     */
    function baseKeys(object) {
      if (!isPrototype(object)) {
        return nativeKeys(object);
      }
      var result = [];
      for (var key in Object(object)) {
        if (hasOwnProperty.call(object, key) && key != 'constructor') {
          result.push(key);
        }
      }
      return result;
    }

    /**
     * The base implementation of `_.rest` which doesn't validate or coerce arguments.
     *
     * @private
     * @param {Function} func The function to apply a rest parameter to.
     * @param {number} [start=func.length-1] The start position of the rest parameter.
     * @returns {Function} Returns the new function.
     */
    function baseRest(func, start) {
      start = nativeMax(start === undefined ? func.length - 1 : start, 0);
      return function () {
        var args = arguments,
            index = -1,
            length = nativeMax(args.length - start, 0),
            array = Array(length);

        while (++index < length) {
          array[index] = args[start + index];
        }
        index = -1;
        var otherArgs = Array(start + 1);
        while (++index < start) {
          otherArgs[index] = args[index];
        }
        otherArgs[start] = array;
        return apply(func, this, otherArgs);
      };
    }

    /**
     * Copies properties of `source` to `object`.
     *
     * @private
     * @param {Object} source The object to copy properties from.
     * @param {Array} props The property identifiers to copy.
     * @param {Object} [object={}] The object to copy properties to.
     * @param {Function} [customizer] The function to customize copied values.
     * @returns {Object} Returns `object`.
     */
    function copyObject(source, props, object, customizer) {
      object || (object = {});

      var index = -1,
          length = props.length;

      while (++index < length) {
        var key = props[index];

        var newValue = customizer ? customizer(object[key], source[key], key, object, source) : undefined;

        assignValue(object, key, newValue === undefined ? source[key] : newValue);
      }
      return object;
    }

    /**
     * Creates a function like `_.assign`.
     *
     * @private
     * @param {Function} assigner The function to assign values.
     * @returns {Function} Returns the new assigner function.
     */
    function createAssigner(assigner) {
      return baseRest(function (object, sources) {
        var index = -1,
            length = sources.length,
            customizer = length > 1 ? sources[length - 1] : undefined,
            guard = length > 2 ? sources[2] : undefined;

        customizer = assigner.length > 3 && typeof customizer == 'function' ? (length--, customizer) : undefined;

        if (guard && isIterateeCall(sources[0], sources[1], guard)) {
          customizer = length < 3 ? undefined : customizer;
          length = 1;
        }
        object = Object(object);
        while (++index < length) {
          var source = sources[index];
          if (source) {
            assigner(object, source, index, customizer);
          }
        }
        return object;
      });
    }

    /**
     * Checks if `value` is a valid array-like index.
     *
     * @private
     * @param {*} value The value to check.
     * @param {number} [length=MAX_SAFE_INTEGER] The upper bounds of a valid index.
     * @returns {boolean} Returns `true` if `value` is a valid index, else `false`.
     */
    function isIndex(value, length) {
      length = length == null ? MAX_SAFE_INTEGER : length;
      return !!length && (typeof value == 'number' || reIsUint.test(value)) && value > -1 && value % 1 == 0 && value < length;
    }

    /**
     * Checks if the given arguments are from an iteratee call.
     *
     * @private
     * @param {*} value The potential iteratee value argument.
     * @param {*} index The potential iteratee index or key argument.
     * @param {*} object The potential iteratee object argument.
     * @returns {boolean} Returns `true` if the arguments are from an iteratee call,
     *  else `false`.
     */
    function isIterateeCall(value, index, object) {
      if (!isObject(object)) {
        return false;
      }
      var type = typeof index === "undefined" ? "undefined" : _typeof(index);
      if (type == 'number' ? isArrayLike(object) && isIndex(index, object.length) : type == 'string' && index in object) {
        return eq(object[index], value);
      }
      return false;
    }

    /**
     * Checks if `value` is likely a prototype object.
     *
     * @private
     * @param {*} value The value to check.
     * @returns {boolean} Returns `true` if `value` is a prototype, else `false`.
     */
    function isPrototype(value) {
      var Ctor = value && value.constructor,
          proto = typeof Ctor == 'function' && Ctor.prototype || objectProto;

      return value === proto;
    }

    /**
     * Performs a
     * [`SameValueZero`](http://ecma-international.org/ecma-262/7.0/#sec-samevaluezero)
     * comparison between two values to determine if they are equivalent.
     *
     * @static
     * @memberOf _
     * @since 4.0.0
     * @category Lang
     * @param {*} value The value to compare.
     * @param {*} other The other value to compare.
     * @returns {boolean} Returns `true` if the values are equivalent, else `false`.
     * @example
     *
     * var object = { 'a': 1 };
     * var other = { 'a': 1 };
     *
     * _.eq(object, object);
     * // => true
     *
     * _.eq(object, other);
     * // => false
     *
     * _.eq('a', 'a');
     * // => true
     *
     * _.eq('a', Object('a'));
     * // => false
     *
     * _.eq(NaN, NaN);
     * // => true
     */
    function eq(value, other) {
      return value === other || value !== value && other !== other;
    }

    /**
     * Checks if `value` is likely an `arguments` object.
     *
     * @static
     * @memberOf _
     * @since 0.1.0
     * @category Lang
     * @param {*} value The value to check.
     * @returns {boolean} Returns `true` if `value` is an `arguments` object,
     *  else `false`.
     * @example
     *
     * _.isArguments(function() { return arguments; }());
     * // => true
     *
     * _.isArguments([1, 2, 3]);
     * // => false
     */
    function isArguments(value) {
      // Safari 8.1 makes `arguments.callee` enumerable in strict mode.
      return isArrayLikeObject(value) && hasOwnProperty.call(value, 'callee') && (!propertyIsEnumerable.call(value, 'callee') || objectToString.call(value) == argsTag);
    }

    /**
     * Checks if `value` is classified as an `Array` object.
     *
     * @static
     * @memberOf _
     * @since 0.1.0
     * @category Lang
     * @param {*} value The value to check.
     * @returns {boolean} Returns `true` if `value` is an array, else `false`.
     * @example
     *
     * _.isArray([1, 2, 3]);
     * // => true
     *
     * _.isArray(document.body.children);
     * // => false
     *
     * _.isArray('abc');
     * // => false
     *
     * _.isArray(_.noop);
     * // => false
     */
    var isArray = Array.isArray;

    /**
     * Checks if `value` is array-like. A value is considered array-like if it's
     * not a function and has a `value.length` that's an integer greater than or
     * equal to `0` and less than or equal to `Number.MAX_SAFE_INTEGER`.
     *
     * @static
     * @memberOf _
     * @since 4.0.0
     * @category Lang
     * @param {*} value The value to check.
     * @returns {boolean} Returns `true` if `value` is array-like, else `false`.
     * @example
     *
     * _.isArrayLike([1, 2, 3]);
     * // => true
     *
     * _.isArrayLike(document.body.children);
     * // => true
     *
     * _.isArrayLike('abc');
     * // => true
     *
     * _.isArrayLike(_.noop);
     * // => false
     */
    function isArrayLike(value) {
      return value != null && isLength(value.length) && !isFunction(value);
    }

    /**
     * This method is like `_.isArrayLike` except that it also checks if `value`
     * is an object.
     *
     * @static
     * @memberOf _
     * @since 4.0.0
     * @category Lang
     * @param {*} value The value to check.
     * @returns {boolean} Returns `true` if `value` is an array-like object,
     *  else `false`.
     * @example
     *
     * _.isArrayLikeObject([1, 2, 3]);
     * // => true
     *
     * _.isArrayLikeObject(document.body.children);
     * // => true
     *
     * _.isArrayLikeObject('abc');
     * // => false
     *
     * _.isArrayLikeObject(_.noop);
     * // => false
     */
    function isArrayLikeObject(value) {
      return isObjectLike(value) && isArrayLike(value);
    }

    /**
     * Checks if `value` is classified as a `Function` object.
     *
     * @static
     * @memberOf _
     * @since 0.1.0
     * @category Lang
     * @param {*} value The value to check.
     * @returns {boolean} Returns `true` if `value` is a function, else `false`.
     * @example
     *
     * _.isFunction(_);
     * // => true
     *
     * _.isFunction(/abc/);
     * // => false
     */
    function isFunction(value) {
      // The use of `Object#toString` avoids issues with the `typeof` operator
      // in Safari 8-9 which returns 'object' for typed array and other constructors.
      var tag = isObject(value) ? objectToString.call(value) : '';
      return tag == funcTag || tag == genTag;
    }

    /**
     * Checks if `value` is a valid array-like length.
     *
     * **Note:** This method is loosely based on
     * [`ToLength`](http://ecma-international.org/ecma-262/7.0/#sec-tolength).
     *
     * @static
     * @memberOf _
     * @since 4.0.0
     * @category Lang
     * @param {*} value The value to check.
     * @returns {boolean} Returns `true` if `value` is a valid length, else `false`.
     * @example
     *
     * _.isLength(3);
     * // => true
     *
     * _.isLength(Number.MIN_VALUE);
     * // => false
     *
     * _.isLength(Infinity);
     * // => false
     *
     * _.isLength('3');
     * // => false
     */
    function isLength(value) {
      return typeof value == 'number' && value > -1 && value % 1 == 0 && value <= MAX_SAFE_INTEGER;
    }

    /**
     * Checks if `value` is the
     * [language type](http://www.ecma-international.org/ecma-262/7.0/#sec-ecmascript-language-types)
     * of `Object`. (e.g. arrays, functions, objects, regexes, `new Number(0)`, and `new String('')`)
     *
     * @static
     * @memberOf _
     * @since 0.1.0
     * @category Lang
     * @param {*} value The value to check.
     * @returns {boolean} Returns `true` if `value` is an object, else `false`.
     * @example
     *
     * _.isObject({});
     * // => true
     *
     * _.isObject([1, 2, 3]);
     * // => true
     *
     * _.isObject(_.noop);
     * // => true
     *
     * _.isObject(null);
     * // => false
     */
    function isObject(value) {
      var type = typeof value === "undefined" ? "undefined" : _typeof(value);
      return !!value && (type == 'object' || type == 'function');
    }

    /**
     * Checks if `value` is object-like. A value is object-like if it's not `null`
     * and has a `typeof` result of "object".
     *
     * @static
     * @memberOf _
     * @since 4.0.0
     * @category Lang
     * @param {*} value The value to check.
     * @returns {boolean} Returns `true` if `value` is object-like, else `false`.
     * @example
     *
     * _.isObjectLike({});
     * // => true
     *
     * _.isObjectLike([1, 2, 3]);
     * // => true
     *
     * _.isObjectLike(_.noop);
     * // => false
     *
     * _.isObjectLike(null);
     * // => false
     */
    function isObjectLike(value) {
      return !!value && (typeof value === "undefined" ? "undefined" : _typeof(value)) == 'object';
    }

    /**
     * Assigns own enumerable string keyed properties of source objects to the
     * destination object. Source objects are applied from left to right.
     * Subsequent sources overwrite property assignments of previous sources.
     *
     * **Note:** This method mutates `object` and is loosely based on
     * [`Object.assign`](https://mdn.io/Object/assign).
     *
     * @static
     * @memberOf _
     * @since 0.10.0
     * @category Object
     * @param {Object} object The destination object.
     * @param {...Object} [sources] The source objects.
     * @returns {Object} Returns `object`.
     * @see _.assignIn
     * @example
     *
     * function Foo() {
     *   this.a = 1;
     * }
     *
     * function Bar() {
     *   this.c = 3;
     * }
     *
     * Foo.prototype.b = 2;
     * Bar.prototype.d = 4;
     *
     * _.assign({ 'a': 0 }, new Foo, new Bar);
     * // => { 'a': 1, 'c': 3 }
     */
    var assign = createAssigner(function (object, source) {
      if (nonEnumShadows || isPrototype(source) || isArrayLike(source)) {
        copyObject(source, keys(source), object);
        return;
      }
      for (var key in source) {
        if (hasOwnProperty.call(source, key)) {
          assignValue(object, key, source[key]);
        }
      }
    });

    /**
     * Creates an array of the own enumerable property names of `object`.
     *
     * **Note:** Non-object values are coerced to objects. See the
     * [ES spec](http://ecma-international.org/ecma-262/7.0/#sec-object.keys)
     * for more details.
     *
     * @static
     * @since 0.1.0
     * @memberOf _
     * @category Object
     * @param {Object} object The object to query.
     * @returns {Array} Returns the array of property names.
     * @example
     *
     * function Foo() {
     *   this.a = 1;
     *   this.b = 2;
     * }
     *
     * Foo.prototype.c = 3;
     *
     * _.keys(new Foo);
     * // => ['a', 'b'] (iteration order is not guaranteed)
     *
     * _.keys('hi');
     * // => ['0', '1']
     */
    function keys(object) {
      return isArrayLike(object) ? arrayLikeKeys(object) : baseKeys(object);
    }

    module.exports = assign;
  }, {}], 11: [function (require, module, exports) {
    (function (global) {
      /**
       * lodash (Custom Build) <https://lodash.com/>
       * Build: `lodash modularize exports="npm" -o ./`
       * Copyright jQuery Foundation and other contributors <https://jquery.org/>
       * Released under MIT license <https://lodash.com/license>
       * Based on Underscore.js 1.8.3 <http://underscorejs.org/LICENSE>
       * Copyright Jeremy Ashkenas, DocumentCloud and Investigative Reporters & Editors
       */

      /** Used as the size to enable large array optimizations. */
      var LARGE_ARRAY_SIZE = 200;

      /** Used to stand-in for `undefined` hash values. */
      var HASH_UNDEFINED = '__lodash_hash_undefined__';

      /** Used as references for various `Number` constants. */
      var MAX_SAFE_INTEGER = 9007199254740991;

      /** `Object#toString` result references. */
      var argsTag = '[object Arguments]',
          arrayTag = '[object Array]',
          boolTag = '[object Boolean]',
          dateTag = '[object Date]',
          errorTag = '[object Error]',
          funcTag = '[object Function]',
          genTag = '[object GeneratorFunction]',
          mapTag = '[object Map]',
          numberTag = '[object Number]',
          objectTag = '[object Object]',
          promiseTag = '[object Promise]',
          regexpTag = '[object RegExp]',
          setTag = '[object Set]',
          stringTag = '[object String]',
          symbolTag = '[object Symbol]',
          weakMapTag = '[object WeakMap]';

      var arrayBufferTag = '[object ArrayBuffer]',
          dataViewTag = '[object DataView]',
          float32Tag = '[object Float32Array]',
          float64Tag = '[object Float64Array]',
          int8Tag = '[object Int8Array]',
          int16Tag = '[object Int16Array]',
          int32Tag = '[object Int32Array]',
          uint8Tag = '[object Uint8Array]',
          uint8ClampedTag = '[object Uint8ClampedArray]',
          uint16Tag = '[object Uint16Array]',
          uint32Tag = '[object Uint32Array]';

      /**
       * Used to match `RegExp`
       * [syntax characters](http://ecma-international.org/ecma-262/7.0/#sec-patterns).
       */
      var reRegExpChar = /[\\^$.*+?()[\]{}|]/g;

      /** Used to match `RegExp` flags from their coerced string values. */
      var reFlags = /\w*$/;

      /** Used to detect host constructors (Safari). */
      var reIsHostCtor = /^\[object .+?Constructor\]$/;

      /** Used to detect unsigned integer values. */
      var reIsUint = /^(?:0|[1-9]\d*)$/;

      /** Used to identify `toStringTag` values supported by `_.clone`. */
      var cloneableTags = {};
      cloneableTags[argsTag] = cloneableTags[arrayTag] = cloneableTags[arrayBufferTag] = cloneableTags[dataViewTag] = cloneableTags[boolTag] = cloneableTags[dateTag] = cloneableTags[float32Tag] = cloneableTags[float64Tag] = cloneableTags[int8Tag] = cloneableTags[int16Tag] = cloneableTags[int32Tag] = cloneableTags[mapTag] = cloneableTags[numberTag] = cloneableTags[objectTag] = cloneableTags[regexpTag] = cloneableTags[setTag] = cloneableTags[stringTag] = cloneableTags[symbolTag] = cloneableTags[uint8Tag] = cloneableTags[uint8ClampedTag] = cloneableTags[uint16Tag] = cloneableTags[uint32Tag] = true;
      cloneableTags[errorTag] = cloneableTags[funcTag] = cloneableTags[weakMapTag] = false;

      /** Detect free variable `global` from Node.js. */
      var freeGlobal = (typeof global === "undefined" ? "undefined" : _typeof(global)) == 'object' && global && global.Object === Object && global;

      /** Detect free variable `self`. */
      var freeSelf = (typeof self === "undefined" ? "undefined" : _typeof(self)) == 'object' && self && self.Object === Object && self;

      /** Used as a reference to the global object. */
      var root = freeGlobal || freeSelf || Function('return this')();

      /** Detect free variable `exports`. */
      var freeExports = (typeof exports === "undefined" ? "undefined" : _typeof(exports)) == 'object' && exports && !exports.nodeType && exports;

      /** Detect free variable `module`. */
      var freeModule = freeExports && (typeof module === "undefined" ? "undefined" : _typeof(module)) == 'object' && module && !module.nodeType && module;

      /** Detect the popular CommonJS extension `module.exports`. */
      var moduleExports = freeModule && freeModule.exports === freeExports;

      /**
       * Adds the key-value `pair` to `map`.
       *
       * @private
       * @param {Object} map The map to modify.
       * @param {Array} pair The key-value pair to add.
       * @returns {Object} Returns `map`.
       */
      function addMapEntry(map, pair) {
        // Don't return `map.set` because it's not chainable in IE 11.
        map.set(pair[0], pair[1]);
        return map;
      }

      /**
       * Adds `value` to `set`.
       *
       * @private
       * @param {Object} set The set to modify.
       * @param {*} value The value to add.
       * @returns {Object} Returns `set`.
       */
      function addSetEntry(set, value) {
        // Don't return `set.add` because it's not chainable in IE 11.
        set.add(value);
        return set;
      }

      /**
       * A specialized version of `_.forEach` for arrays without support for
       * iteratee shorthands.
       *
       * @private
       * @param {Array} [array] The array to iterate over.
       * @param {Function} iteratee The function invoked per iteration.
       * @returns {Array} Returns `array`.
       */
      function arrayEach(array, iteratee) {
        var index = -1,
            length = array ? array.length : 0;

        while (++index < length) {
          if (iteratee(array[index], index, array) === false) {
            break;
          }
        }
        return array;
      }

      /**
       * Appends the elements of `values` to `array`.
       *
       * @private
       * @param {Array} array The array to modify.
       * @param {Array} values The values to append.
       * @returns {Array} Returns `array`.
       */
      function arrayPush(array, values) {
        var index = -1,
            length = values.length,
            offset = array.length;

        while (++index < length) {
          array[offset + index] = values[index];
        }
        return array;
      }

      /**
       * A specialized version of `_.reduce` for arrays without support for
       * iteratee shorthands.
       *
       * @private
       * @param {Array} [array] The array to iterate over.
       * @param {Function} iteratee The function invoked per iteration.
       * @param {*} [accumulator] The initial value.
       * @param {boolean} [initAccum] Specify using the first element of `array` as
       *  the initial value.
       * @returns {*} Returns the accumulated value.
       */
      function arrayReduce(array, iteratee, accumulator, initAccum) {
        var index = -1,
            length = array ? array.length : 0;

        if (initAccum && length) {
          accumulator = array[++index];
        }
        while (++index < length) {
          accumulator = iteratee(accumulator, array[index], index, array);
        }
        return accumulator;
      }

      /**
       * The base implementation of `_.times` without support for iteratee shorthands
       * or max array length checks.
       *
       * @private
       * @param {number} n The number of times to invoke `iteratee`.
       * @param {Function} iteratee The function invoked per iteration.
       * @returns {Array} Returns the array of results.
       */
      function baseTimes(n, iteratee) {
        var index = -1,
            result = Array(n);

        while (++index < n) {
          result[index] = iteratee(index);
        }
        return result;
      }

      /**
       * Gets the value at `key` of `object`.
       *
       * @private
       * @param {Object} [object] The object to query.
       * @param {string} key The key of the property to get.
       * @returns {*} Returns the property value.
       */
      function getValue(object, key) {
        return object == null ? undefined : object[key];
      }

      /**
       * Checks if `value` is a host object in IE < 9.
       *
       * @private
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a host object, else `false`.
       */
      function isHostObject(value) {
        // Many host objects are `Object` objects that can coerce to strings
        // despite having improperly defined `toString` methods.
        var result = false;
        if (value != null && typeof value.toString != 'function') {
          try {
            result = !!(value + '');
          } catch (e) {}
        }
        return result;
      }

      /**
       * Converts `map` to its key-value pairs.
       *
       * @private
       * @param {Object} map The map to convert.
       * @returns {Array} Returns the key-value pairs.
       */
      function mapToArray(map) {
        var index = -1,
            result = Array(map.size);

        map.forEach(function (value, key) {
          result[++index] = [key, value];
        });
        return result;
      }

      /**
       * Creates a unary function that invokes `func` with its argument transformed.
       *
       * @private
       * @param {Function} func The function to wrap.
       * @param {Function} transform The argument transform.
       * @returns {Function} Returns the new function.
       */
      function overArg(func, transform) {
        return function (arg) {
          return func(transform(arg));
        };
      }

      /**
       * Converts `set` to an array of its values.
       *
       * @private
       * @param {Object} set The set to convert.
       * @returns {Array} Returns the values.
       */
      function setToArray(set) {
        var index = -1,
            result = Array(set.size);

        set.forEach(function (value) {
          result[++index] = value;
        });
        return result;
      }

      /** Used for built-in method references. */
      var arrayProto = Array.prototype,
          funcProto = Function.prototype,
          objectProto = Object.prototype;

      /** Used to detect overreaching core-js shims. */
      var coreJsData = root['__core-js_shared__'];

      /** Used to detect methods masquerading as native. */
      var maskSrcKey = function () {
        var uid = /[^.]+$/.exec(coreJsData && coreJsData.keys && coreJsData.keys.IE_PROTO || '');
        return uid ? 'Symbol(src)_1.' + uid : '';
      }();

      /** Used to resolve the decompiled source of functions. */
      var funcToString = funcProto.toString;

      /** Used to check objects for own properties. */
      var hasOwnProperty = objectProto.hasOwnProperty;

      /**
       * Used to resolve the
       * [`toStringTag`](http://ecma-international.org/ecma-262/7.0/#sec-object.prototype.tostring)
       * of values.
       */
      var objectToString = objectProto.toString;

      /** Used to detect if a method is native. */
      var reIsNative = RegExp('^' + funcToString.call(hasOwnProperty).replace(reRegExpChar, '\\$&').replace(/hasOwnProperty|(function).*?(?=\\\()| for .+?(?=\\\])/g, '$1.*?') + '$');

      /** Built-in value references. */
      var Buffer = moduleExports ? root.Buffer : undefined,
          _Symbol = root.Symbol,
          Uint8Array = root.Uint8Array,
          getPrototype = overArg(Object.getPrototypeOf, Object),
          objectCreate = Object.create,
          propertyIsEnumerable = objectProto.propertyIsEnumerable,
          splice = arrayProto.splice;

      /* Built-in method references for those with the same name as other `lodash` methods. */
      var nativeGetSymbols = Object.getOwnPropertySymbols,
          nativeIsBuffer = Buffer ? Buffer.isBuffer : undefined,
          nativeKeys = overArg(Object.keys, Object);

      /* Built-in method references that are verified to be native. */
      var DataView = getNative(root, 'DataView'),
          Map = getNative(root, 'Map'),
          Promise = getNative(root, 'Promise'),
          Set = getNative(root, 'Set'),
          WeakMap = getNative(root, 'WeakMap'),
          nativeCreate = getNative(Object, 'create');

      /** Used to detect maps, sets, and weakmaps. */
      var dataViewCtorString = toSource(DataView),
          mapCtorString = toSource(Map),
          promiseCtorString = toSource(Promise),
          setCtorString = toSource(Set),
          weakMapCtorString = toSource(WeakMap);

      /** Used to convert symbols to primitives and strings. */
      var symbolProto = _Symbol ? _Symbol.prototype : undefined,
          symbolValueOf = symbolProto ? symbolProto.valueOf : undefined;

      /**
       * Creates a hash object.
       *
       * @private
       * @constructor
       * @param {Array} [entries] The key-value pairs to cache.
       */
      function Hash(entries) {
        var index = -1,
            length = entries ? entries.length : 0;

        this.clear();
        while (++index < length) {
          var entry = entries[index];
          this.set(entry[0], entry[1]);
        }
      }

      /**
       * Removes all key-value entries from the hash.
       *
       * @private
       * @name clear
       * @memberOf Hash
       */
      function hashClear() {
        this.__data__ = nativeCreate ? nativeCreate(null) : {};
      }

      /**
       * Removes `key` and its value from the hash.
       *
       * @private
       * @name delete
       * @memberOf Hash
       * @param {Object} hash The hash to modify.
       * @param {string} key The key of the value to remove.
       * @returns {boolean} Returns `true` if the entry was removed, else `false`.
       */
      function hashDelete(key) {
        return this.has(key) && delete this.__data__[key];
      }

      /**
       * Gets the hash value for `key`.
       *
       * @private
       * @name get
       * @memberOf Hash
       * @param {string} key The key of the value to get.
       * @returns {*} Returns the entry value.
       */
      function hashGet(key) {
        var data = this.__data__;
        if (nativeCreate) {
          var result = data[key];
          return result === HASH_UNDEFINED ? undefined : result;
        }
        return hasOwnProperty.call(data, key) ? data[key] : undefined;
      }

      /**
       * Checks if a hash value for `key` exists.
       *
       * @private
       * @name has
       * @memberOf Hash
       * @param {string} key The key of the entry to check.
       * @returns {boolean} Returns `true` if an entry for `key` exists, else `false`.
       */
      function hashHas(key) {
        var data = this.__data__;
        return nativeCreate ? data[key] !== undefined : hasOwnProperty.call(data, key);
      }

      /**
       * Sets the hash `key` to `value`.
       *
       * @private
       * @name set
       * @memberOf Hash
       * @param {string} key The key of the value to set.
       * @param {*} value The value to set.
       * @returns {Object} Returns the hash instance.
       */
      function hashSet(key, value) {
        var data = this.__data__;
        data[key] = nativeCreate && value === undefined ? HASH_UNDEFINED : value;
        return this;
      }

      // Add methods to `Hash`.
      Hash.prototype.clear = hashClear;
      Hash.prototype['delete'] = hashDelete;
      Hash.prototype.get = hashGet;
      Hash.prototype.has = hashHas;
      Hash.prototype.set = hashSet;

      /**
       * Creates an list cache object.
       *
       * @private
       * @constructor
       * @param {Array} [entries] The key-value pairs to cache.
       */
      function ListCache(entries) {
        var index = -1,
            length = entries ? entries.length : 0;

        this.clear();
        while (++index < length) {
          var entry = entries[index];
          this.set(entry[0], entry[1]);
        }
      }

      /**
       * Removes all key-value entries from the list cache.
       *
       * @private
       * @name clear
       * @memberOf ListCache
       */
      function listCacheClear() {
        this.__data__ = [];
      }

      /**
       * Removes `key` and its value from the list cache.
       *
       * @private
       * @name delete
       * @memberOf ListCache
       * @param {string} key The key of the value to remove.
       * @returns {boolean} Returns `true` if the entry was removed, else `false`.
       */
      function listCacheDelete(key) {
        var data = this.__data__,
            index = assocIndexOf(data, key);

        if (index < 0) {
          return false;
        }
        var lastIndex = data.length - 1;
        if (index == lastIndex) {
          data.pop();
        } else {
          splice.call(data, index, 1);
        }
        return true;
      }

      /**
       * Gets the list cache value for `key`.
       *
       * @private
       * @name get
       * @memberOf ListCache
       * @param {string} key The key of the value to get.
       * @returns {*} Returns the entry value.
       */
      function listCacheGet(key) {
        var data = this.__data__,
            index = assocIndexOf(data, key);

        return index < 0 ? undefined : data[index][1];
      }

      /**
       * Checks if a list cache value for `key` exists.
       *
       * @private
       * @name has
       * @memberOf ListCache
       * @param {string} key The key of the entry to check.
       * @returns {boolean} Returns `true` if an entry for `key` exists, else `false`.
       */
      function listCacheHas(key) {
        return assocIndexOf(this.__data__, key) > -1;
      }

      /**
       * Sets the list cache `key` to `value`.
       *
       * @private
       * @name set
       * @memberOf ListCache
       * @param {string} key The key of the value to set.
       * @param {*} value The value to set.
       * @returns {Object} Returns the list cache instance.
       */
      function listCacheSet(key, value) {
        var data = this.__data__,
            index = assocIndexOf(data, key);

        if (index < 0) {
          data.push([key, value]);
        } else {
          data[index][1] = value;
        }
        return this;
      }

      // Add methods to `ListCache`.
      ListCache.prototype.clear = listCacheClear;
      ListCache.prototype['delete'] = listCacheDelete;
      ListCache.prototype.get = listCacheGet;
      ListCache.prototype.has = listCacheHas;
      ListCache.prototype.set = listCacheSet;

      /**
       * Creates a map cache object to store key-value pairs.
       *
       * @private
       * @constructor
       * @param {Array} [entries] The key-value pairs to cache.
       */
      function MapCache(entries) {
        var index = -1,
            length = entries ? entries.length : 0;

        this.clear();
        while (++index < length) {
          var entry = entries[index];
          this.set(entry[0], entry[1]);
        }
      }

      /**
       * Removes all key-value entries from the map.
       *
       * @private
       * @name clear
       * @memberOf MapCache
       */
      function mapCacheClear() {
        this.__data__ = {
          'hash': new Hash(),
          'map': new (Map || ListCache)(),
          'string': new Hash()
        };
      }

      /**
       * Removes `key` and its value from the map.
       *
       * @private
       * @name delete
       * @memberOf MapCache
       * @param {string} key The key of the value to remove.
       * @returns {boolean} Returns `true` if the entry was removed, else `false`.
       */
      function mapCacheDelete(key) {
        return getMapData(this, key)['delete'](key);
      }

      /**
       * Gets the map value for `key`.
       *
       * @private
       * @name get
       * @memberOf MapCache
       * @param {string} key The key of the value to get.
       * @returns {*} Returns the entry value.
       */
      function mapCacheGet(key) {
        return getMapData(this, key).get(key);
      }

      /**
       * Checks if a map value for `key` exists.
       *
       * @private
       * @name has
       * @memberOf MapCache
       * @param {string} key The key of the entry to check.
       * @returns {boolean} Returns `true` if an entry for `key` exists, else `false`.
       */
      function mapCacheHas(key) {
        return getMapData(this, key).has(key);
      }

      /**
       * Sets the map `key` to `value`.
       *
       * @private
       * @name set
       * @memberOf MapCache
       * @param {string} key The key of the value to set.
       * @param {*} value The value to set.
       * @returns {Object} Returns the map cache instance.
       */
      function mapCacheSet(key, value) {
        getMapData(this, key).set(key, value);
        return this;
      }

      // Add methods to `MapCache`.
      MapCache.prototype.clear = mapCacheClear;
      MapCache.prototype['delete'] = mapCacheDelete;
      MapCache.prototype.get = mapCacheGet;
      MapCache.prototype.has = mapCacheHas;
      MapCache.prototype.set = mapCacheSet;

      /**
       * Creates a stack cache object to store key-value pairs.
       *
       * @private
       * @constructor
       * @param {Array} [entries] The key-value pairs to cache.
       */
      function Stack(entries) {
        this.__data__ = new ListCache(entries);
      }

      /**
       * Removes all key-value entries from the stack.
       *
       * @private
       * @name clear
       * @memberOf Stack
       */
      function stackClear() {
        this.__data__ = new ListCache();
      }

      /**
       * Removes `key` and its value from the stack.
       *
       * @private
       * @name delete
       * @memberOf Stack
       * @param {string} key The key of the value to remove.
       * @returns {boolean} Returns `true` if the entry was removed, else `false`.
       */
      function stackDelete(key) {
        return this.__data__['delete'](key);
      }

      /**
       * Gets the stack value for `key`.
       *
       * @private
       * @name get
       * @memberOf Stack
       * @param {string} key The key of the value to get.
       * @returns {*} Returns the entry value.
       */
      function stackGet(key) {
        return this.__data__.get(key);
      }

      /**
       * Checks if a stack value for `key` exists.
       *
       * @private
       * @name has
       * @memberOf Stack
       * @param {string} key The key of the entry to check.
       * @returns {boolean} Returns `true` if an entry for `key` exists, else `false`.
       */
      function stackHas(key) {
        return this.__data__.has(key);
      }

      /**
       * Sets the stack `key` to `value`.
       *
       * @private
       * @name set
       * @memberOf Stack
       * @param {string} key The key of the value to set.
       * @param {*} value The value to set.
       * @returns {Object} Returns the stack cache instance.
       */
      function stackSet(key, value) {
        var cache = this.__data__;
        if (cache instanceof ListCache) {
          var pairs = cache.__data__;
          if (!Map || pairs.length < LARGE_ARRAY_SIZE - 1) {
            pairs.push([key, value]);
            return this;
          }
          cache = this.__data__ = new MapCache(pairs);
        }
        cache.set(key, value);
        return this;
      }

      // Add methods to `Stack`.
      Stack.prototype.clear = stackClear;
      Stack.prototype['delete'] = stackDelete;
      Stack.prototype.get = stackGet;
      Stack.prototype.has = stackHas;
      Stack.prototype.set = stackSet;

      /**
       * Creates an array of the enumerable property names of the array-like `value`.
       *
       * @private
       * @param {*} value The value to query.
       * @param {boolean} inherited Specify returning inherited property names.
       * @returns {Array} Returns the array of property names.
       */
      function arrayLikeKeys(value, inherited) {
        // Safari 8.1 makes `arguments.callee` enumerable in strict mode.
        // Safari 9 makes `arguments.length` enumerable in strict mode.
        var result = isArray(value) || isArguments(value) ? baseTimes(value.length, String) : [];

        var length = result.length,
            skipIndexes = !!length;

        for (var key in value) {
          if ((inherited || hasOwnProperty.call(value, key)) && !(skipIndexes && (key == 'length' || isIndex(key, length)))) {
            result.push(key);
          }
        }
        return result;
      }

      /**
       * Assigns `value` to `key` of `object` if the existing value is not equivalent
       * using [`SameValueZero`](http://ecma-international.org/ecma-262/7.0/#sec-samevaluezero)
       * for equality comparisons.
       *
       * @private
       * @param {Object} object The object to modify.
       * @param {string} key The key of the property to assign.
       * @param {*} value The value to assign.
       */
      function assignValue(object, key, value) {
        var objValue = object[key];
        if (!(hasOwnProperty.call(object, key) && eq(objValue, value)) || value === undefined && !(key in object)) {
          object[key] = value;
        }
      }

      /**
       * Gets the index at which the `key` is found in `array` of key-value pairs.
       *
       * @private
       * @param {Array} array The array to inspect.
       * @param {*} key The key to search for.
       * @returns {number} Returns the index of the matched value, else `-1`.
       */
      function assocIndexOf(array, key) {
        var length = array.length;
        while (length--) {
          if (eq(array[length][0], key)) {
            return length;
          }
        }
        return -1;
      }

      /**
       * The base implementation of `_.assign` without support for multiple sources
       * or `customizer` functions.
       *
       * @private
       * @param {Object} object The destination object.
       * @param {Object} source The source object.
       * @returns {Object} Returns `object`.
       */
      function baseAssign(object, source) {
        return object && copyObject(source, keys(source), object);
      }

      /**
       * The base implementation of `_.clone` and `_.cloneDeep` which tracks
       * traversed objects.
       *
       * @private
       * @param {*} value The value to clone.
       * @param {boolean} [isDeep] Specify a deep clone.
       * @param {boolean} [isFull] Specify a clone including symbols.
       * @param {Function} [customizer] The function to customize cloning.
       * @param {string} [key] The key of `value`.
       * @param {Object} [object] The parent object of `value`.
       * @param {Object} [stack] Tracks traversed objects and their clone counterparts.
       * @returns {*} Returns the cloned value.
       */
      function baseClone(value, isDeep, isFull, customizer, key, object, stack) {
        var result;
        if (customizer) {
          result = object ? customizer(value, key, object, stack) : customizer(value);
        }
        if (result !== undefined) {
          return result;
        }
        if (!isObject(value)) {
          return value;
        }
        var isArr = isArray(value);
        if (isArr) {
          result = initCloneArray(value);
          if (!isDeep) {
            return copyArray(value, result);
          }
        } else {
          var tag = getTag(value),
              isFunc = tag == funcTag || tag == genTag;

          if (isBuffer(value)) {
            return cloneBuffer(value, isDeep);
          }
          if (tag == objectTag || tag == argsTag || isFunc && !object) {
            if (isHostObject(value)) {
              return object ? value : {};
            }
            result = initCloneObject(isFunc ? {} : value);
            if (!isDeep) {
              return copySymbols(value, baseAssign(result, value));
            }
          } else {
            if (!cloneableTags[tag]) {
              return object ? value : {};
            }
            result = initCloneByTag(value, tag, baseClone, isDeep);
          }
        }
        // Check for circular references and return its corresponding clone.
        stack || (stack = new Stack());
        var stacked = stack.get(value);
        if (stacked) {
          return stacked;
        }
        stack.set(value, result);

        if (!isArr) {
          var props = isFull ? getAllKeys(value) : keys(value);
        }
        arrayEach(props || value, function (subValue, key) {
          if (props) {
            key = subValue;
            subValue = value[key];
          }
          // Recursively populate clone (susceptible to call stack limits).
          assignValue(result, key, baseClone(subValue, isDeep, isFull, customizer, key, value, stack));
        });
        return result;
      }

      /**
       * The base implementation of `_.create` without support for assigning
       * properties to the created object.
       *
       * @private
       * @param {Object} prototype The object to inherit from.
       * @returns {Object} Returns the new object.
       */
      function baseCreate(proto) {
        return isObject(proto) ? objectCreate(proto) : {};
      }

      /**
       * The base implementation of `getAllKeys` and `getAllKeysIn` which uses
       * `keysFunc` and `symbolsFunc` to get the enumerable property names and
       * symbols of `object`.
       *
       * @private
       * @param {Object} object The object to query.
       * @param {Function} keysFunc The function to get the keys of `object`.
       * @param {Function} symbolsFunc The function to get the symbols of `object`.
       * @returns {Array} Returns the array of property names and symbols.
       */
      function baseGetAllKeys(object, keysFunc, symbolsFunc) {
        var result = keysFunc(object);
        return isArray(object) ? result : arrayPush(result, symbolsFunc(object));
      }

      /**
       * The base implementation of `getTag`.
       *
       * @private
       * @param {*} value The value to query.
       * @returns {string} Returns the `toStringTag`.
       */
      function baseGetTag(value) {
        return objectToString.call(value);
      }

      /**
       * The base implementation of `_.isNative` without bad shim checks.
       *
       * @private
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a native function,
       *  else `false`.
       */
      function baseIsNative(value) {
        if (!isObject(value) || isMasked(value)) {
          return false;
        }
        var pattern = isFunction(value) || isHostObject(value) ? reIsNative : reIsHostCtor;
        return pattern.test(toSource(value));
      }

      /**
       * The base implementation of `_.keys` which doesn't treat sparse arrays as dense.
       *
       * @private
       * @param {Object} object The object to query.
       * @returns {Array} Returns the array of property names.
       */
      function baseKeys(object) {
        if (!isPrototype(object)) {
          return nativeKeys(object);
        }
        var result = [];
        for (var key in Object(object)) {
          if (hasOwnProperty.call(object, key) && key != 'constructor') {
            result.push(key);
          }
        }
        return result;
      }

      /**
       * Creates a clone of  `buffer`.
       *
       * @private
       * @param {Buffer} buffer The buffer to clone.
       * @param {boolean} [isDeep] Specify a deep clone.
       * @returns {Buffer} Returns the cloned buffer.
       */
      function cloneBuffer(buffer, isDeep) {
        if (isDeep) {
          return buffer.slice();
        }
        var result = new buffer.constructor(buffer.length);
        buffer.copy(result);
        return result;
      }

      /**
       * Creates a clone of `arrayBuffer`.
       *
       * @private
       * @param {ArrayBuffer} arrayBuffer The array buffer to clone.
       * @returns {ArrayBuffer} Returns the cloned array buffer.
       */
      function cloneArrayBuffer(arrayBuffer) {
        var result = new arrayBuffer.constructor(arrayBuffer.byteLength);
        new Uint8Array(result).set(new Uint8Array(arrayBuffer));
        return result;
      }

      /**
       * Creates a clone of `dataView`.
       *
       * @private
       * @param {Object} dataView The data view to clone.
       * @param {boolean} [isDeep] Specify a deep clone.
       * @returns {Object} Returns the cloned data view.
       */
      function cloneDataView(dataView, isDeep) {
        var buffer = isDeep ? cloneArrayBuffer(dataView.buffer) : dataView.buffer;
        return new dataView.constructor(buffer, dataView.byteOffset, dataView.byteLength);
      }

      /**
       * Creates a clone of `map`.
       *
       * @private
       * @param {Object} map The map to clone.
       * @param {Function} cloneFunc The function to clone values.
       * @param {boolean} [isDeep] Specify a deep clone.
       * @returns {Object} Returns the cloned map.
       */
      function cloneMap(map, isDeep, cloneFunc) {
        var array = isDeep ? cloneFunc(mapToArray(map), true) : mapToArray(map);
        return arrayReduce(array, addMapEntry, new map.constructor());
      }

      /**
       * Creates a clone of `regexp`.
       *
       * @private
       * @param {Object} regexp The regexp to clone.
       * @returns {Object} Returns the cloned regexp.
       */
      function cloneRegExp(regexp) {
        var result = new regexp.constructor(regexp.source, reFlags.exec(regexp));
        result.lastIndex = regexp.lastIndex;
        return result;
      }

      /**
       * Creates a clone of `set`.
       *
       * @private
       * @param {Object} set The set to clone.
       * @param {Function} cloneFunc The function to clone values.
       * @param {boolean} [isDeep] Specify a deep clone.
       * @returns {Object} Returns the cloned set.
       */
      function cloneSet(set, isDeep, cloneFunc) {
        var array = isDeep ? cloneFunc(setToArray(set), true) : setToArray(set);
        return arrayReduce(array, addSetEntry, new set.constructor());
      }

      /**
       * Creates a clone of the `symbol` object.
       *
       * @private
       * @param {Object} symbol The symbol object to clone.
       * @returns {Object} Returns the cloned symbol object.
       */
      function cloneSymbol(symbol) {
        return symbolValueOf ? Object(symbolValueOf.call(symbol)) : {};
      }

      /**
       * Creates a clone of `typedArray`.
       *
       * @private
       * @param {Object} typedArray The typed array to clone.
       * @param {boolean} [isDeep] Specify a deep clone.
       * @returns {Object} Returns the cloned typed array.
       */
      function cloneTypedArray(typedArray, isDeep) {
        var buffer = isDeep ? cloneArrayBuffer(typedArray.buffer) : typedArray.buffer;
        return new typedArray.constructor(buffer, typedArray.byteOffset, typedArray.length);
      }

      /**
       * Copies the values of `source` to `array`.
       *
       * @private
       * @param {Array} source The array to copy values from.
       * @param {Array} [array=[]] The array to copy values to.
       * @returns {Array} Returns `array`.
       */
      function copyArray(source, array) {
        var index = -1,
            length = source.length;

        array || (array = Array(length));
        while (++index < length) {
          array[index] = source[index];
        }
        return array;
      }

      /**
       * Copies properties of `source` to `object`.
       *
       * @private
       * @param {Object} source The object to copy properties from.
       * @param {Array} props The property identifiers to copy.
       * @param {Object} [object={}] The object to copy properties to.
       * @param {Function} [customizer] The function to customize copied values.
       * @returns {Object} Returns `object`.
       */
      function copyObject(source, props, object, customizer) {
        object || (object = {});

        var index = -1,
            length = props.length;

        while (++index < length) {
          var key = props[index];

          var newValue = customizer ? customizer(object[key], source[key], key, object, source) : undefined;

          assignValue(object, key, newValue === undefined ? source[key] : newValue);
        }
        return object;
      }

      /**
       * Copies own symbol properties of `source` to `object`.
       *
       * @private
       * @param {Object} source The object to copy symbols from.
       * @param {Object} [object={}] The object to copy symbols to.
       * @returns {Object} Returns `object`.
       */
      function copySymbols(source, object) {
        return copyObject(source, getSymbols(source), object);
      }

      /**
       * Creates an array of own enumerable property names and symbols of `object`.
       *
       * @private
       * @param {Object} object The object to query.
       * @returns {Array} Returns the array of property names and symbols.
       */
      function getAllKeys(object) {
        return baseGetAllKeys(object, keys, getSymbols);
      }

      /**
       * Gets the data for `map`.
       *
       * @private
       * @param {Object} map The map to query.
       * @param {string} key The reference key.
       * @returns {*} Returns the map data.
       */
      function getMapData(map, key) {
        var data = map.__data__;
        return isKeyable(key) ? data[typeof key == 'string' ? 'string' : 'hash'] : data.map;
      }

      /**
       * Gets the native function at `key` of `object`.
       *
       * @private
       * @param {Object} object The object to query.
       * @param {string} key The key of the method to get.
       * @returns {*} Returns the function if it's native, else `undefined`.
       */
      function getNative(object, key) {
        var value = getValue(object, key);
        return baseIsNative(value) ? value : undefined;
      }

      /**
       * Creates an array of the own enumerable symbol properties of `object`.
       *
       * @private
       * @param {Object} object The object to query.
       * @returns {Array} Returns the array of symbols.
       */
      var getSymbols = nativeGetSymbols ? overArg(nativeGetSymbols, Object) : stubArray;

      /**
       * Gets the `toStringTag` of `value`.
       *
       * @private
       * @param {*} value The value to query.
       * @returns {string} Returns the `toStringTag`.
       */
      var getTag = baseGetTag;

      // Fallback for data views, maps, sets, and weak maps in IE 11,
      // for data views in Edge < 14, and promises in Node.js.
      if (DataView && getTag(new DataView(new ArrayBuffer(1))) != dataViewTag || Map && getTag(new Map()) != mapTag || Promise && getTag(Promise.resolve()) != promiseTag || Set && getTag(new Set()) != setTag || WeakMap && getTag(new WeakMap()) != weakMapTag) {
        getTag = function getTag(value) {
          var result = objectToString.call(value),
              Ctor = result == objectTag ? value.constructor : undefined,
              ctorString = Ctor ? toSource(Ctor) : undefined;

          if (ctorString) {
            switch (ctorString) {
              case dataViewCtorString:
                return dataViewTag;
              case mapCtorString:
                return mapTag;
              case promiseCtorString:
                return promiseTag;
              case setCtorString:
                return setTag;
              case weakMapCtorString:
                return weakMapTag;
            }
          }
          return result;
        };
      }

      /**
       * Initializes an array clone.
       *
       * @private
       * @param {Array} array The array to clone.
       * @returns {Array} Returns the initialized clone.
       */
      function initCloneArray(array) {
        var length = array.length,
            result = array.constructor(length);

        // Add properties assigned by `RegExp#exec`.
        if (length && typeof array[0] == 'string' && hasOwnProperty.call(array, 'index')) {
          result.index = array.index;
          result.input = array.input;
        }
        return result;
      }

      /**
       * Initializes an object clone.
       *
       * @private
       * @param {Object} object The object to clone.
       * @returns {Object} Returns the initialized clone.
       */
      function initCloneObject(object) {
        return typeof object.constructor == 'function' && !isPrototype(object) ? baseCreate(getPrototype(object)) : {};
      }

      /**
       * Initializes an object clone based on its `toStringTag`.
       *
       * **Note:** This function only supports cloning values with tags of
       * `Boolean`, `Date`, `Error`, `Number`, `RegExp`, or `String`.
       *
       * @private
       * @param {Object} object The object to clone.
       * @param {string} tag The `toStringTag` of the object to clone.
       * @param {Function} cloneFunc The function to clone values.
       * @param {boolean} [isDeep] Specify a deep clone.
       * @returns {Object} Returns the initialized clone.
       */
      function initCloneByTag(object, tag, cloneFunc, isDeep) {
        var Ctor = object.constructor;
        switch (tag) {
          case arrayBufferTag:
            return cloneArrayBuffer(object);

          case boolTag:
          case dateTag:
            return new Ctor(+object);

          case dataViewTag:
            return cloneDataView(object, isDeep);

          case float32Tag:case float64Tag:
          case int8Tag:case int16Tag:case int32Tag:
          case uint8Tag:case uint8ClampedTag:case uint16Tag:case uint32Tag:
            return cloneTypedArray(object, isDeep);

          case mapTag:
            return cloneMap(object, isDeep, cloneFunc);

          case numberTag:
          case stringTag:
            return new Ctor(object);

          case regexpTag:
            return cloneRegExp(object);

          case setTag:
            return cloneSet(object, isDeep, cloneFunc);

          case symbolTag:
            return cloneSymbol(object);
        }
      }

      /**
       * Checks if `value` is a valid array-like index.
       *
       * @private
       * @param {*} value The value to check.
       * @param {number} [length=MAX_SAFE_INTEGER] The upper bounds of a valid index.
       * @returns {boolean} Returns `true` if `value` is a valid index, else `false`.
       */
      function isIndex(value, length) {
        length = length == null ? MAX_SAFE_INTEGER : length;
        return !!length && (typeof value == 'number' || reIsUint.test(value)) && value > -1 && value % 1 == 0 && value < length;
      }

      /**
       * Checks if `value` is suitable for use as unique object key.
       *
       * @private
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is suitable, else `false`.
       */
      function isKeyable(value) {
        var type = typeof value === "undefined" ? "undefined" : _typeof(value);
        return type == 'string' || type == 'number' || type == 'symbol' || type == 'boolean' ? value !== '__proto__' : value === null;
      }

      /**
       * Checks if `func` has its source masked.
       *
       * @private
       * @param {Function} func The function to check.
       * @returns {boolean} Returns `true` if `func` is masked, else `false`.
       */
      function isMasked(func) {
        return !!maskSrcKey && maskSrcKey in func;
      }

      /**
       * Checks if `value` is likely a prototype object.
       *
       * @private
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a prototype, else `false`.
       */
      function isPrototype(value) {
        var Ctor = value && value.constructor,
            proto = typeof Ctor == 'function' && Ctor.prototype || objectProto;

        return value === proto;
      }

      /**
       * Converts `func` to its source code.
       *
       * @private
       * @param {Function} func The function to process.
       * @returns {string} Returns the source code.
       */
      function toSource(func) {
        if (func != null) {
          try {
            return funcToString.call(func);
          } catch (e) {}
          try {
            return func + '';
          } catch (e) {}
        }
        return '';
      }

      /**
       * Creates a shallow clone of `value`.
       *
       * **Note:** This method is loosely based on the
       * [structured clone algorithm](https://mdn.io/Structured_clone_algorithm)
       * and supports cloning arrays, array buffers, booleans, date objects, maps,
       * numbers, `Object` objects, regexes, sets, strings, symbols, and typed
       * arrays. The own enumerable properties of `arguments` objects are cloned
       * as plain objects. An empty object is returned for uncloneable values such
       * as error objects, functions, DOM nodes, and WeakMaps.
       *
       * @static
       * @memberOf _
       * @since 0.1.0
       * @category Lang
       * @param {*} value The value to clone.
       * @returns {*} Returns the cloned value.
       * @see _.cloneDeep
       * @example
       *
       * var objects = [{ 'a': 1 }, { 'b': 2 }];
       *
       * var shallow = _.clone(objects);
       * console.log(shallow[0] === objects[0]);
       * // => true
       */
      function clone(value) {
        return baseClone(value, false, true);
      }

      /**
       * Performs a
       * [`SameValueZero`](http://ecma-international.org/ecma-262/7.0/#sec-samevaluezero)
       * comparison between two values to determine if they are equivalent.
       *
       * @static
       * @memberOf _
       * @since 4.0.0
       * @category Lang
       * @param {*} value The value to compare.
       * @param {*} other The other value to compare.
       * @returns {boolean} Returns `true` if the values are equivalent, else `false`.
       * @example
       *
       * var object = { 'a': 1 };
       * var other = { 'a': 1 };
       *
       * _.eq(object, object);
       * // => true
       *
       * _.eq(object, other);
       * // => false
       *
       * _.eq('a', 'a');
       * // => true
       *
       * _.eq('a', Object('a'));
       * // => false
       *
       * _.eq(NaN, NaN);
       * // => true
       */
      function eq(value, other) {
        return value === other || value !== value && other !== other;
      }

      /**
       * Checks if `value` is likely an `arguments` object.
       *
       * @static
       * @memberOf _
       * @since 0.1.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is an `arguments` object,
       *  else `false`.
       * @example
       *
       * _.isArguments(function() { return arguments; }());
       * // => true
       *
       * _.isArguments([1, 2, 3]);
       * // => false
       */
      function isArguments(value) {
        // Safari 8.1 makes `arguments.callee` enumerable in strict mode.
        return isArrayLikeObject(value) && hasOwnProperty.call(value, 'callee') && (!propertyIsEnumerable.call(value, 'callee') || objectToString.call(value) == argsTag);
      }

      /**
       * Checks if `value` is classified as an `Array` object.
       *
       * @static
       * @memberOf _
       * @since 0.1.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is an array, else `false`.
       * @example
       *
       * _.isArray([1, 2, 3]);
       * // => true
       *
       * _.isArray(document.body.children);
       * // => false
       *
       * _.isArray('abc');
       * // => false
       *
       * _.isArray(_.noop);
       * // => false
       */
      var isArray = Array.isArray;

      /**
       * Checks if `value` is array-like. A value is considered array-like if it's
       * not a function and has a `value.length` that's an integer greater than or
       * equal to `0` and less than or equal to `Number.MAX_SAFE_INTEGER`.
       *
       * @static
       * @memberOf _
       * @since 4.0.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is array-like, else `false`.
       * @example
       *
       * _.isArrayLike([1, 2, 3]);
       * // => true
       *
       * _.isArrayLike(document.body.children);
       * // => true
       *
       * _.isArrayLike('abc');
       * // => true
       *
       * _.isArrayLike(_.noop);
       * // => false
       */
      function isArrayLike(value) {
        return value != null && isLength(value.length) && !isFunction(value);
      }

      /**
       * This method is like `_.isArrayLike` except that it also checks if `value`
       * is an object.
       *
       * @static
       * @memberOf _
       * @since 4.0.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is an array-like object,
       *  else `false`.
       * @example
       *
       * _.isArrayLikeObject([1, 2, 3]);
       * // => true
       *
       * _.isArrayLikeObject(document.body.children);
       * // => true
       *
       * _.isArrayLikeObject('abc');
       * // => false
       *
       * _.isArrayLikeObject(_.noop);
       * // => false
       */
      function isArrayLikeObject(value) {
        return isObjectLike(value) && isArrayLike(value);
      }

      /**
       * Checks if `value` is a buffer.
       *
       * @static
       * @memberOf _
       * @since 4.3.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a buffer, else `false`.
       * @example
       *
       * _.isBuffer(new Buffer(2));
       * // => true
       *
       * _.isBuffer(new Uint8Array(2));
       * // => false
       */
      var isBuffer = nativeIsBuffer || stubFalse;

      /**
       * Checks if `value` is classified as a `Function` object.
       *
       * @static
       * @memberOf _
       * @since 0.1.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a function, else `false`.
       * @example
       *
       * _.isFunction(_);
       * // => true
       *
       * _.isFunction(/abc/);
       * // => false
       */
      function isFunction(value) {
        // The use of `Object#toString` avoids issues with the `typeof` operator
        // in Safari 8-9 which returns 'object' for typed array and other constructors.
        var tag = isObject(value) ? objectToString.call(value) : '';
        return tag == funcTag || tag == genTag;
      }

      /**
       * Checks if `value` is a valid array-like length.
       *
       * **Note:** This method is loosely based on
       * [`ToLength`](http://ecma-international.org/ecma-262/7.0/#sec-tolength).
       *
       * @static
       * @memberOf _
       * @since 4.0.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a valid length, else `false`.
       * @example
       *
       * _.isLength(3);
       * // => true
       *
       * _.isLength(Number.MIN_VALUE);
       * // => false
       *
       * _.isLength(Infinity);
       * // => false
       *
       * _.isLength('3');
       * // => false
       */
      function isLength(value) {
        return typeof value == 'number' && value > -1 && value % 1 == 0 && value <= MAX_SAFE_INTEGER;
      }

      /**
       * Checks if `value` is the
       * [language type](http://www.ecma-international.org/ecma-262/7.0/#sec-ecmascript-language-types)
       * of `Object`. (e.g. arrays, functions, objects, regexes, `new Number(0)`, and `new String('')`)
       *
       * @static
       * @memberOf _
       * @since 0.1.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is an object, else `false`.
       * @example
       *
       * _.isObject({});
       * // => true
       *
       * _.isObject([1, 2, 3]);
       * // => true
       *
       * _.isObject(_.noop);
       * // => true
       *
       * _.isObject(null);
       * // => false
       */
      function isObject(value) {
        var type = typeof value === "undefined" ? "undefined" : _typeof(value);
        return !!value && (type == 'object' || type == 'function');
      }

      /**
       * Checks if `value` is object-like. A value is object-like if it's not `null`
       * and has a `typeof` result of "object".
       *
       * @static
       * @memberOf _
       * @since 4.0.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is object-like, else `false`.
       * @example
       *
       * _.isObjectLike({});
       * // => true
       *
       * _.isObjectLike([1, 2, 3]);
       * // => true
       *
       * _.isObjectLike(_.noop);
       * // => false
       *
       * _.isObjectLike(null);
       * // => false
       */
      function isObjectLike(value) {
        return !!value && (typeof value === "undefined" ? "undefined" : _typeof(value)) == 'object';
      }

      /**
       * Creates an array of the own enumerable property names of `object`.
       *
       * **Note:** Non-object values are coerced to objects. See the
       * [ES spec](http://ecma-international.org/ecma-262/7.0/#sec-object.keys)
       * for more details.
       *
       * @static
       * @since 0.1.0
       * @memberOf _
       * @category Object
       * @param {Object} object The object to query.
       * @returns {Array} Returns the array of property names.
       * @example
       *
       * function Foo() {
       *   this.a = 1;
       *   this.b = 2;
       * }
       *
       * Foo.prototype.c = 3;
       *
       * _.keys(new Foo);
       * // => ['a', 'b'] (iteration order is not guaranteed)
       *
       * _.keys('hi');
       * // => ['0', '1']
       */
      function keys(object) {
        return isArrayLike(object) ? arrayLikeKeys(object) : baseKeys(object);
      }

      /**
       * This method returns a new empty array.
       *
       * @static
       * @memberOf _
       * @since 4.13.0
       * @category Util
       * @returns {Array} Returns the new empty array.
       * @example
       *
       * var arrays = _.times(2, _.stubArray);
       *
       * console.log(arrays);
       * // => [[], []]
       *
       * console.log(arrays[0] === arrays[1]);
       * // => false
       */
      function stubArray() {
        return [];
      }

      /**
       * This method returns `false`.
       *
       * @static
       * @memberOf _
       * @since 4.13.0
       * @category Util
       * @returns {boolean} Returns `false`.
       * @example
       *
       * _.times(2, _.stubFalse);
       * // => [false, false]
       */
      function stubFalse() {
        return false;
      }

      module.exports = clone;
    }).call(this, typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {});
  }, {}], 12: [function (require, module, exports) {
    /**
     * lodash (Custom Build) <https://lodash.com/>
     * Build: `lodash modularize exports="npm" -o ./`
     * Copyright jQuery Foundation and other contributors <https://jquery.org/>
     * Released under MIT license <https://lodash.com/license>
     * Based on Underscore.js 1.8.3 <http://underscorejs.org/LICENSE>
     * Copyright Jeremy Ashkenas, DocumentCloud and Investigative Reporters & Editors
     */

    /** Used as references for various `Number` constants. */
    var MAX_SAFE_INTEGER = 9007199254740991;

    /** `Object#toString` result references. */
    var argsTag = '[object Arguments]',
        funcTag = '[object Function]',
        genTag = '[object GeneratorFunction]';

    /** Used to detect unsigned integer values. */
    var reIsUint = /^(?:0|[1-9]\d*)$/;

    /**
     * A specialized version of `_.forEach` for arrays without support for
     * iteratee shorthands.
     *
     * @private
     * @param {Array} [array] The array to iterate over.
     * @param {Function} iteratee The function invoked per iteration.
     * @returns {Array} Returns `array`.
     */
    function arrayEach(array, iteratee) {
      var index = -1,
          length = array ? array.length : 0;

      while (++index < length) {
        if (iteratee(array[index], index, array) === false) {
          break;
        }
      }
      return array;
    }

    /**
     * The base implementation of `_.times` without support for iteratee shorthands
     * or max array length checks.
     *
     * @private
     * @param {number} n The number of times to invoke `iteratee`.
     * @param {Function} iteratee The function invoked per iteration.
     * @returns {Array} Returns the array of results.
     */
    function baseTimes(n, iteratee) {
      var index = -1,
          result = Array(n);

      while (++index < n) {
        result[index] = iteratee(index);
      }
      return result;
    }

    /**
     * Creates a unary function that invokes `func` with its argument transformed.
     *
     * @private
     * @param {Function} func The function to wrap.
     * @param {Function} transform The argument transform.
     * @returns {Function} Returns the new function.
     */
    function overArg(func, transform) {
      return function (arg) {
        return func(transform(arg));
      };
    }

    /** Used for built-in method references. */
    var objectProto = Object.prototype;

    /** Used to check objects for own properties. */
    var hasOwnProperty = objectProto.hasOwnProperty;

    /**
     * Used to resolve the
     * [`toStringTag`](http://ecma-international.org/ecma-262/7.0/#sec-object.prototype.tostring)
     * of values.
     */
    var objectToString = objectProto.toString;

    /** Built-in value references. */
    var propertyIsEnumerable = objectProto.propertyIsEnumerable;

    /* Built-in method references for those with the same name as other `lodash` methods. */
    var nativeKeys = overArg(Object.keys, Object);

    /**
     * Creates an array of the enumerable property names of the array-like `value`.
     *
     * @private
     * @param {*} value The value to query.
     * @param {boolean} inherited Specify returning inherited property names.
     * @returns {Array} Returns the array of property names.
     */
    function arrayLikeKeys(value, inherited) {
      // Safari 8.1 makes `arguments.callee` enumerable in strict mode.
      // Safari 9 makes `arguments.length` enumerable in strict mode.
      var result = isArray(value) || isArguments(value) ? baseTimes(value.length, String) : [];

      var length = result.length,
          skipIndexes = !!length;

      for (var key in value) {
        if ((inherited || hasOwnProperty.call(value, key)) && !(skipIndexes && (key == 'length' || isIndex(key, length)))) {
          result.push(key);
        }
      }
      return result;
    }

    /**
     * The base implementation of `_.forEach` without support for iteratee shorthands.
     *
     * @private
     * @param {Array|Object} collection The collection to iterate over.
     * @param {Function} iteratee The function invoked per iteration.
     * @returns {Array|Object} Returns `collection`.
     */
    var baseEach = createBaseEach(baseForOwn);

    /**
     * The base implementation of `baseForOwn` which iterates over `object`
     * properties returned by `keysFunc` and invokes `iteratee` for each property.
     * Iteratee functions may exit iteration early by explicitly returning `false`.
     *
     * @private
     * @param {Object} object The object to iterate over.
     * @param {Function} iteratee The function invoked per iteration.
     * @param {Function} keysFunc The function to get the keys of `object`.
     * @returns {Object} Returns `object`.
     */
    var baseFor = createBaseFor();

    /**
     * The base implementation of `_.forOwn` without support for iteratee shorthands.
     *
     * @private
     * @param {Object} object The object to iterate over.
     * @param {Function} iteratee The function invoked per iteration.
     * @returns {Object} Returns `object`.
     */
    function baseForOwn(object, iteratee) {
      return object && baseFor(object, iteratee, keys);
    }

    /**
     * The base implementation of `_.keys` which doesn't treat sparse arrays as dense.
     *
     * @private
     * @param {Object} object The object to query.
     * @returns {Array} Returns the array of property names.
     */
    function baseKeys(object) {
      if (!isPrototype(object)) {
        return nativeKeys(object);
      }
      var result = [];
      for (var key in Object(object)) {
        if (hasOwnProperty.call(object, key) && key != 'constructor') {
          result.push(key);
        }
      }
      return result;
    }

    /**
     * Creates a `baseEach` or `baseEachRight` function.
     *
     * @private
     * @param {Function} eachFunc The function to iterate over a collection.
     * @param {boolean} [fromRight] Specify iterating from right to left.
     * @returns {Function} Returns the new base function.
     */
    function createBaseEach(eachFunc, fromRight) {
      return function (collection, iteratee) {
        if (collection == null) {
          return collection;
        }
        if (!isArrayLike(collection)) {
          return eachFunc(collection, iteratee);
        }
        var length = collection.length,
            index = fromRight ? length : -1,
            iterable = Object(collection);

        while (fromRight ? index-- : ++index < length) {
          if (iteratee(iterable[index], index, iterable) === false) {
            break;
          }
        }
        return collection;
      };
    }

    /**
     * Creates a base function for methods like `_.forIn` and `_.forOwn`.
     *
     * @private
     * @param {boolean} [fromRight] Specify iterating from right to left.
     * @returns {Function} Returns the new base function.
     */
    function createBaseFor(fromRight) {
      return function (object, iteratee, keysFunc) {
        var index = -1,
            iterable = Object(object),
            props = keysFunc(object),
            length = props.length;

        while (length--) {
          var key = props[fromRight ? length : ++index];
          if (iteratee(iterable[key], key, iterable) === false) {
            break;
          }
        }
        return object;
      };
    }

    /**
     * Checks if `value` is a valid array-like index.
     *
     * @private
     * @param {*} value The value to check.
     * @param {number} [length=MAX_SAFE_INTEGER] The upper bounds of a valid index.
     * @returns {boolean} Returns `true` if `value` is a valid index, else `false`.
     */
    function isIndex(value, length) {
      length = length == null ? MAX_SAFE_INTEGER : length;
      return !!length && (typeof value == 'number' || reIsUint.test(value)) && value > -1 && value % 1 == 0 && value < length;
    }

    /**
     * Checks if `value` is likely a prototype object.
     *
     * @private
     * @param {*} value The value to check.
     * @returns {boolean} Returns `true` if `value` is a prototype, else `false`.
     */
    function isPrototype(value) {
      var Ctor = value && value.constructor,
          proto = typeof Ctor == 'function' && Ctor.prototype || objectProto;

      return value === proto;
    }

    /**
     * Iterates over elements of `collection` and invokes `iteratee` for each element.
     * The iteratee is invoked with three arguments: (value, index|key, collection).
     * Iteratee functions may exit iteration early by explicitly returning `false`.
     *
     * **Note:** As with other "Collections" methods, objects with a "length"
     * property are iterated like arrays. To avoid this behavior use `_.forIn`
     * or `_.forOwn` for object iteration.
     *
     * @static
     * @memberOf _
     * @since 0.1.0
     * @alias each
     * @category Collection
     * @param {Array|Object} collection The collection to iterate over.
     * @param {Function} [iteratee=_.identity] The function invoked per iteration.
     * @returns {Array|Object} Returns `collection`.
     * @see _.forEachRight
     * @example
     *
     * _([1, 2]).forEach(function(value) {
     *   console.log(value);
     * });
     * // => Logs `1` then `2`.
     *
     * _.forEach({ 'a': 1, 'b': 2 }, function(value, key) {
     *   console.log(key);
     * });
     * // => Logs 'a' then 'b' (iteration order is not guaranteed).
     */
    function forEach(collection, iteratee) {
      var func = isArray(collection) ? arrayEach : baseEach;
      return func(collection, typeof iteratee == 'function' ? iteratee : identity);
    }

    /**
     * Checks if `value` is likely an `arguments` object.
     *
     * @static
     * @memberOf _
     * @since 0.1.0
     * @category Lang
     * @param {*} value The value to check.
     * @returns {boolean} Returns `true` if `value` is an `arguments` object,
     *  else `false`.
     * @example
     *
     * _.isArguments(function() { return arguments; }());
     * // => true
     *
     * _.isArguments([1, 2, 3]);
     * // => false
     */
    function isArguments(value) {
      // Safari 8.1 makes `arguments.callee` enumerable in strict mode.
      return isArrayLikeObject(value) && hasOwnProperty.call(value, 'callee') && (!propertyIsEnumerable.call(value, 'callee') || objectToString.call(value) == argsTag);
    }

    /**
     * Checks if `value` is classified as an `Array` object.
     *
     * @static
     * @memberOf _
     * @since 0.1.0
     * @category Lang
     * @param {*} value The value to check.
     * @returns {boolean} Returns `true` if `value` is an array, else `false`.
     * @example
     *
     * _.isArray([1, 2, 3]);
     * // => true
     *
     * _.isArray(document.body.children);
     * // => false
     *
     * _.isArray('abc');
     * // => false
     *
     * _.isArray(_.noop);
     * // => false
     */
    var isArray = Array.isArray;

    /**
     * Checks if `value` is array-like. A value is considered array-like if it's
     * not a function and has a `value.length` that's an integer greater than or
     * equal to `0` and less than or equal to `Number.MAX_SAFE_INTEGER`.
     *
     * @static
     * @memberOf _
     * @since 4.0.0
     * @category Lang
     * @param {*} value The value to check.
     * @returns {boolean} Returns `true` if `value` is array-like, else `false`.
     * @example
     *
     * _.isArrayLike([1, 2, 3]);
     * // => true
     *
     * _.isArrayLike(document.body.children);
     * // => true
     *
     * _.isArrayLike('abc');
     * // => true
     *
     * _.isArrayLike(_.noop);
     * // => false
     */
    function isArrayLike(value) {
      return value != null && isLength(value.length) && !isFunction(value);
    }

    /**
     * This method is like `_.isArrayLike` except that it also checks if `value`
     * is an object.
     *
     * @static
     * @memberOf _
     * @since 4.0.0
     * @category Lang
     * @param {*} value The value to check.
     * @returns {boolean} Returns `true` if `value` is an array-like object,
     *  else `false`.
     * @example
     *
     * _.isArrayLikeObject([1, 2, 3]);
     * // => true
     *
     * _.isArrayLikeObject(document.body.children);
     * // => true
     *
     * _.isArrayLikeObject('abc');
     * // => false
     *
     * _.isArrayLikeObject(_.noop);
     * // => false
     */
    function isArrayLikeObject(value) {
      return isObjectLike(value) && isArrayLike(value);
    }

    /**
     * Checks if `value` is classified as a `Function` object.
     *
     * @static
     * @memberOf _
     * @since 0.1.0
     * @category Lang
     * @param {*} value The value to check.
     * @returns {boolean} Returns `true` if `value` is a function, else `false`.
     * @example
     *
     * _.isFunction(_);
     * // => true
     *
     * _.isFunction(/abc/);
     * // => false
     */
    function isFunction(value) {
      // The use of `Object#toString` avoids issues with the `typeof` operator
      // in Safari 8-9 which returns 'object' for typed array and other constructors.
      var tag = isObject(value) ? objectToString.call(value) : '';
      return tag == funcTag || tag == genTag;
    }

    /**
     * Checks if `value` is a valid array-like length.
     *
     * **Note:** This method is loosely based on
     * [`ToLength`](http://ecma-international.org/ecma-262/7.0/#sec-tolength).
     *
     * @static
     * @memberOf _
     * @since 4.0.0
     * @category Lang
     * @param {*} value The value to check.
     * @returns {boolean} Returns `true` if `value` is a valid length, else `false`.
     * @example
     *
     * _.isLength(3);
     * // => true
     *
     * _.isLength(Number.MIN_VALUE);
     * // => false
     *
     * _.isLength(Infinity);
     * // => false
     *
     * _.isLength('3');
     * // => false
     */
    function isLength(value) {
      return typeof value == 'number' && value > -1 && value % 1 == 0 && value <= MAX_SAFE_INTEGER;
    }

    /**
     * Checks if `value` is the
     * [language type](http://www.ecma-international.org/ecma-262/7.0/#sec-ecmascript-language-types)
     * of `Object`. (e.g. arrays, functions, objects, regexes, `new Number(0)`, and `new String('')`)
     *
     * @static
     * @memberOf _
     * @since 0.1.0
     * @category Lang
     * @param {*} value The value to check.
     * @returns {boolean} Returns `true` if `value` is an object, else `false`.
     * @example
     *
     * _.isObject({});
     * // => true
     *
     * _.isObject([1, 2, 3]);
     * // => true
     *
     * _.isObject(_.noop);
     * // => true
     *
     * _.isObject(null);
     * // => false
     */
    function isObject(value) {
      var type = typeof value === "undefined" ? "undefined" : _typeof(value);
      return !!value && (type == 'object' || type == 'function');
    }

    /**
     * Checks if `value` is object-like. A value is object-like if it's not `null`
     * and has a `typeof` result of "object".
     *
     * @static
     * @memberOf _
     * @since 4.0.0
     * @category Lang
     * @param {*} value The value to check.
     * @returns {boolean} Returns `true` if `value` is object-like, else `false`.
     * @example
     *
     * _.isObjectLike({});
     * // => true
     *
     * _.isObjectLike([1, 2, 3]);
     * // => true
     *
     * _.isObjectLike(_.noop);
     * // => false
     *
     * _.isObjectLike(null);
     * // => false
     */
    function isObjectLike(value) {
      return !!value && (typeof value === "undefined" ? "undefined" : _typeof(value)) == 'object';
    }

    /**
     * Creates an array of the own enumerable property names of `object`.
     *
     * **Note:** Non-object values are coerced to objects. See the
     * [ES spec](http://ecma-international.org/ecma-262/7.0/#sec-object.keys)
     * for more details.
     *
     * @static
     * @since 0.1.0
     * @memberOf _
     * @category Object
     * @param {Object} object The object to query.
     * @returns {Array} Returns the array of property names.
     * @example
     *
     * function Foo() {
     *   this.a = 1;
     *   this.b = 2;
     * }
     *
     * Foo.prototype.c = 3;
     *
     * _.keys(new Foo);
     * // => ['a', 'b'] (iteration order is not guaranteed)
     *
     * _.keys('hi');
     * // => ['0', '1']
     */
    function keys(object) {
      return isArrayLike(object) ? arrayLikeKeys(object) : baseKeys(object);
    }

    /**
     * This method returns the first argument it receives.
     *
     * @static
     * @since 0.1.0
     * @memberOf _
     * @category Util
     * @param {*} value Any value.
     * @returns {*} Returns `value`.
     * @example
     *
     * var object = { 'a': 1 };
     *
     * console.log(_.identity(object) === object);
     * // => true
     */
    function identity(value) {
      return value;
    }

    module.exports = forEach;
  }, {}], 13: [function (require, module, exports) {
    (function (global) {
      /**
       * lodash (Custom Build) <https://lodash.com/>
       * Build: `lodash modularize exports="npm" -o ./`
       * Copyright jQuery Foundation and other contributors <https://jquery.org/>
       * Released under MIT license <https://lodash.com/license>
       * Based on Underscore.js 1.8.3 <http://underscorejs.org/LICENSE>
       * Copyright Jeremy Ashkenas, DocumentCloud and Investigative Reporters & Editors
       */

      /** Used as references for various `Number` constants. */
      var MAX_SAFE_INTEGER = 9007199254740991;

      /** `Object#toString` result references. */
      var argsTag = '[object Arguments]',
          funcTag = '[object Function]',
          genTag = '[object GeneratorFunction]',
          mapTag = '[object Map]',
          objectTag = '[object Object]',
          promiseTag = '[object Promise]',
          setTag = '[object Set]',
          weakMapTag = '[object WeakMap]';

      var dataViewTag = '[object DataView]';

      /**
       * Used to match `RegExp`
       * [syntax characters](http://ecma-international.org/ecma-262/7.0/#sec-patterns).
       */
      var reRegExpChar = /[\\^$.*+?()[\]{}|]/g;

      /** Used to detect host constructors (Safari). */
      var reIsHostCtor = /^\[object .+?Constructor\]$/;

      /** Detect free variable `global` from Node.js. */
      var freeGlobal = (typeof global === "undefined" ? "undefined" : _typeof(global)) == 'object' && global && global.Object === Object && global;

      /** Detect free variable `self`. */
      var freeSelf = (typeof self === "undefined" ? "undefined" : _typeof(self)) == 'object' && self && self.Object === Object && self;

      /** Used as a reference to the global object. */
      var root = freeGlobal || freeSelf || Function('return this')();

      /** Detect free variable `exports`. */
      var freeExports = (typeof exports === "undefined" ? "undefined" : _typeof(exports)) == 'object' && exports && !exports.nodeType && exports;

      /** Detect free variable `module`. */
      var freeModule = freeExports && (typeof module === "undefined" ? "undefined" : _typeof(module)) == 'object' && module && !module.nodeType && module;

      /** Detect the popular CommonJS extension `module.exports`. */
      var moduleExports = freeModule && freeModule.exports === freeExports;

      /**
       * Gets the value at `key` of `object`.
       *
       * @private
       * @param {Object} [object] The object to query.
       * @param {string} key The key of the property to get.
       * @returns {*} Returns the property value.
       */
      function getValue(object, key) {
        return object == null ? undefined : object[key];
      }

      /**
       * Checks if `value` is a host object in IE < 9.
       *
       * @private
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a host object, else `false`.
       */
      function isHostObject(value) {
        // Many host objects are `Object` objects that can coerce to strings
        // despite having improperly defined `toString` methods.
        var result = false;
        if (value != null && typeof value.toString != 'function') {
          try {
            result = !!(value + '');
          } catch (e) {}
        }
        return result;
      }

      /**
       * Creates a unary function that invokes `func` with its argument transformed.
       *
       * @private
       * @param {Function} func The function to wrap.
       * @param {Function} transform The argument transform.
       * @returns {Function} Returns the new function.
       */
      function overArg(func, transform) {
        return function (arg) {
          return func(transform(arg));
        };
      }

      /** Used for built-in method references. */
      var funcProto = Function.prototype,
          objectProto = Object.prototype;

      /** Used to detect overreaching core-js shims. */
      var coreJsData = root['__core-js_shared__'];

      /** Used to detect methods masquerading as native. */
      var maskSrcKey = function () {
        var uid = /[^.]+$/.exec(coreJsData && coreJsData.keys && coreJsData.keys.IE_PROTO || '');
        return uid ? 'Symbol(src)_1.' + uid : '';
      }();

      /** Used to resolve the decompiled source of functions. */
      var funcToString = funcProto.toString;

      /** Used to check objects for own properties. */
      var hasOwnProperty = objectProto.hasOwnProperty;

      /**
       * Used to resolve the
       * [`toStringTag`](http://ecma-international.org/ecma-262/7.0/#sec-object.prototype.tostring)
       * of values.
       */
      var objectToString = objectProto.toString;

      /** Used to detect if a method is native. */
      var reIsNative = RegExp('^' + funcToString.call(hasOwnProperty).replace(reRegExpChar, '\\$&').replace(/hasOwnProperty|(function).*?(?=\\\()| for .+?(?=\\\])/g, '$1.*?') + '$');

      /** Built-in value references. */
      var Buffer = moduleExports ? root.Buffer : undefined,
          propertyIsEnumerable = objectProto.propertyIsEnumerable;

      /* Built-in method references for those with the same name as other `lodash` methods. */
      var nativeIsBuffer = Buffer ? Buffer.isBuffer : undefined,
          nativeKeys = overArg(Object.keys, Object);

      /* Built-in method references that are verified to be native. */
      var DataView = getNative(root, 'DataView'),
          Map = getNative(root, 'Map'),
          Promise = getNative(root, 'Promise'),
          Set = getNative(root, 'Set'),
          WeakMap = getNative(root, 'WeakMap');

      /** Detect if properties shadowing those on `Object.prototype` are non-enumerable. */
      var nonEnumShadows = !propertyIsEnumerable.call({ 'valueOf': 1 }, 'valueOf');

      /** Used to detect maps, sets, and weakmaps. */
      var dataViewCtorString = toSource(DataView),
          mapCtorString = toSource(Map),
          promiseCtorString = toSource(Promise),
          setCtorString = toSource(Set),
          weakMapCtorString = toSource(WeakMap);

      /**
       * The base implementation of `getTag`.
       *
       * @private
       * @param {*} value The value to query.
       * @returns {string} Returns the `toStringTag`.
       */
      function baseGetTag(value) {
        return objectToString.call(value);
      }

      /**
       * The base implementation of `_.isNative` without bad shim checks.
       *
       * @private
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a native function,
       *  else `false`.
       */
      function baseIsNative(value) {
        if (!isObject(value) || isMasked(value)) {
          return false;
        }
        var pattern = isFunction(value) || isHostObject(value) ? reIsNative : reIsHostCtor;
        return pattern.test(toSource(value));
      }

      /**
       * Gets the native function at `key` of `object`.
       *
       * @private
       * @param {Object} object The object to query.
       * @param {string} key The key of the method to get.
       * @returns {*} Returns the function if it's native, else `undefined`.
       */
      function getNative(object, key) {
        var value = getValue(object, key);
        return baseIsNative(value) ? value : undefined;
      }

      /**
       * Gets the `toStringTag` of `value`.
       *
       * @private
       * @param {*} value The value to query.
       * @returns {string} Returns the `toStringTag`.
       */
      var getTag = baseGetTag;

      // Fallback for data views, maps, sets, and weak maps in IE 11,
      // for data views in Edge < 14, and promises in Node.js.
      if (DataView && getTag(new DataView(new ArrayBuffer(1))) != dataViewTag || Map && getTag(new Map()) != mapTag || Promise && getTag(Promise.resolve()) != promiseTag || Set && getTag(new Set()) != setTag || WeakMap && getTag(new WeakMap()) != weakMapTag) {
        getTag = function getTag(value) {
          var result = objectToString.call(value),
              Ctor = result == objectTag ? value.constructor : undefined,
              ctorString = Ctor ? toSource(Ctor) : undefined;

          if (ctorString) {
            switch (ctorString) {
              case dataViewCtorString:
                return dataViewTag;
              case mapCtorString:
                return mapTag;
              case promiseCtorString:
                return promiseTag;
              case setCtorString:
                return setTag;
              case weakMapCtorString:
                return weakMapTag;
            }
          }
          return result;
        };
      }

      /**
       * Checks if `func` has its source masked.
       *
       * @private
       * @param {Function} func The function to check.
       * @returns {boolean} Returns `true` if `func` is masked, else `false`.
       */
      function isMasked(func) {
        return !!maskSrcKey && maskSrcKey in func;
      }

      /**
       * Checks if `value` is likely a prototype object.
       *
       * @private
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a prototype, else `false`.
       */
      function isPrototype(value) {
        var Ctor = value && value.constructor,
            proto = typeof Ctor == 'function' && Ctor.prototype || objectProto;

        return value === proto;
      }

      /**
       * Converts `func` to its source code.
       *
       * @private
       * @param {Function} func The function to process.
       * @returns {string} Returns the source code.
       */
      function toSource(func) {
        if (func != null) {
          try {
            return funcToString.call(func);
          } catch (e) {}
          try {
            return func + '';
          } catch (e) {}
        }
        return '';
      }

      /**
       * Checks if `value` is likely an `arguments` object.
       *
       * @static
       * @memberOf _
       * @since 0.1.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is an `arguments` object,
       *  else `false`.
       * @example
       *
       * _.isArguments(function() { return arguments; }());
       * // => true
       *
       * _.isArguments([1, 2, 3]);
       * // => false
       */
      function isArguments(value) {
        // Safari 8.1 makes `arguments.callee` enumerable in strict mode.
        return isArrayLikeObject(value) && hasOwnProperty.call(value, 'callee') && (!propertyIsEnumerable.call(value, 'callee') || objectToString.call(value) == argsTag);
      }

      /**
       * Checks if `value` is classified as an `Array` object.
       *
       * @static
       * @memberOf _
       * @since 0.1.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is an array, else `false`.
       * @example
       *
       * _.isArray([1, 2, 3]);
       * // => true
       *
       * _.isArray(document.body.children);
       * // => false
       *
       * _.isArray('abc');
       * // => false
       *
       * _.isArray(_.noop);
       * // => false
       */
      var isArray = Array.isArray;

      /**
       * Checks if `value` is array-like. A value is considered array-like if it's
       * not a function and has a `value.length` that's an integer greater than or
       * equal to `0` and less than or equal to `Number.MAX_SAFE_INTEGER`.
       *
       * @static
       * @memberOf _
       * @since 4.0.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is array-like, else `false`.
       * @example
       *
       * _.isArrayLike([1, 2, 3]);
       * // => true
       *
       * _.isArrayLike(document.body.children);
       * // => true
       *
       * _.isArrayLike('abc');
       * // => true
       *
       * _.isArrayLike(_.noop);
       * // => false
       */
      function isArrayLike(value) {
        return value != null && isLength(value.length) && !isFunction(value);
      }

      /**
       * This method is like `_.isArrayLike` except that it also checks if `value`
       * is an object.
       *
       * @static
       * @memberOf _
       * @since 4.0.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is an array-like object,
       *  else `false`.
       * @example
       *
       * _.isArrayLikeObject([1, 2, 3]);
       * // => true
       *
       * _.isArrayLikeObject(document.body.children);
       * // => true
       *
       * _.isArrayLikeObject('abc');
       * // => false
       *
       * _.isArrayLikeObject(_.noop);
       * // => false
       */
      function isArrayLikeObject(value) {
        return isObjectLike(value) && isArrayLike(value);
      }

      /**
       * Checks if `value` is a buffer.
       *
       * @static
       * @memberOf _
       * @since 4.3.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a buffer, else `false`.
       * @example
       *
       * _.isBuffer(new Buffer(2));
       * // => true
       *
       * _.isBuffer(new Uint8Array(2));
       * // => false
       */
      var isBuffer = nativeIsBuffer || stubFalse;

      /**
       * Checks if `value` is an empty object, collection, map, or set.
       *
       * Objects are considered empty if they have no own enumerable string keyed
       * properties.
       *
       * Array-like values such as `arguments` objects, arrays, buffers, strings, or
       * jQuery-like collections are considered empty if they have a `length` of `0`.
       * Similarly, maps and sets are considered empty if they have a `size` of `0`.
       *
       * @static
       * @memberOf _
       * @since 0.1.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is empty, else `false`.
       * @example
       *
       * _.isEmpty(null);
       * // => true
       *
       * _.isEmpty(true);
       * // => true
       *
       * _.isEmpty(1);
       * // => true
       *
       * _.isEmpty([1, 2, 3]);
       * // => false
       *
       * _.isEmpty({ 'a': 1 });
       * // => false
       */
      function isEmpty(value) {
        if (isArrayLike(value) && (isArray(value) || typeof value == 'string' || typeof value.splice == 'function' || isBuffer(value) || isArguments(value))) {
          return !value.length;
        }
        var tag = getTag(value);
        if (tag == mapTag || tag == setTag) {
          return !value.size;
        }
        if (nonEnumShadows || isPrototype(value)) {
          return !nativeKeys(value).length;
        }
        for (var key in value) {
          if (hasOwnProperty.call(value, key)) {
            return false;
          }
        }
        return true;
      }

      /**
       * Checks if `value` is classified as a `Function` object.
       *
       * @static
       * @memberOf _
       * @since 0.1.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a function, else `false`.
       * @example
       *
       * _.isFunction(_);
       * // => true
       *
       * _.isFunction(/abc/);
       * // => false
       */
      function isFunction(value) {
        // The use of `Object#toString` avoids issues with the `typeof` operator
        // in Safari 8-9 which returns 'object' for typed array and other constructors.
        var tag = isObject(value) ? objectToString.call(value) : '';
        return tag == funcTag || tag == genTag;
      }

      /**
       * Checks if `value` is a valid array-like length.
       *
       * **Note:** This method is loosely based on
       * [`ToLength`](http://ecma-international.org/ecma-262/7.0/#sec-tolength).
       *
       * @static
       * @memberOf _
       * @since 4.0.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a valid length, else `false`.
       * @example
       *
       * _.isLength(3);
       * // => true
       *
       * _.isLength(Number.MIN_VALUE);
       * // => false
       *
       * _.isLength(Infinity);
       * // => false
       *
       * _.isLength('3');
       * // => false
       */
      function isLength(value) {
        return typeof value == 'number' && value > -1 && value % 1 == 0 && value <= MAX_SAFE_INTEGER;
      }

      /**
       * Checks if `value` is the
       * [language type](http://www.ecma-international.org/ecma-262/7.0/#sec-ecmascript-language-types)
       * of `Object`. (e.g. arrays, functions, objects, regexes, `new Number(0)`, and `new String('')`)
       *
       * @static
       * @memberOf _
       * @since 0.1.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is an object, else `false`.
       * @example
       *
       * _.isObject({});
       * // => true
       *
       * _.isObject([1, 2, 3]);
       * // => true
       *
       * _.isObject(_.noop);
       * // => true
       *
       * _.isObject(null);
       * // => false
       */
      function isObject(value) {
        var type = typeof value === "undefined" ? "undefined" : _typeof(value);
        return !!value && (type == 'object' || type == 'function');
      }

      /**
       * Checks if `value` is object-like. A value is object-like if it's not `null`
       * and has a `typeof` result of "object".
       *
       * @static
       * @memberOf _
       * @since 4.0.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is object-like, else `false`.
       * @example
       *
       * _.isObjectLike({});
       * // => true
       *
       * _.isObjectLike([1, 2, 3]);
       * // => true
       *
       * _.isObjectLike(_.noop);
       * // => false
       *
       * _.isObjectLike(null);
       * // => false
       */
      function isObjectLike(value) {
        return !!value && (typeof value === "undefined" ? "undefined" : _typeof(value)) == 'object';
      }

      /**
       * This method returns `false`.
       *
       * @static
       * @memberOf _
       * @since 4.13.0
       * @category Util
       * @returns {boolean} Returns `false`.
       * @example
       *
       * _.times(2, _.stubFalse);
       * // => [false, false]
       */
      function stubFalse() {
        return false;
      }

      module.exports = isEmpty;
    }).call(this, typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {});
  }, {}], 14: [function (require, module, exports) {
    (function (global) {
      /**
       * Lodash (Custom Build) <https://lodash.com/>
       * Build: `lodash modularize exports="npm" -o ./`
       * Copyright JS Foundation and other contributors <https://js.foundation/>
       * Released under MIT license <https://lodash.com/license>
       * Based on Underscore.js 1.8.3 <http://underscorejs.org/LICENSE>
       * Copyright Jeremy Ashkenas, DocumentCloud and Investigative Reporters & Editors
       */

      /** Used as the size to enable large array optimizations. */
      var LARGE_ARRAY_SIZE = 200;

      /** Used to stand-in for `undefined` hash values. */
      var HASH_UNDEFINED = '__lodash_hash_undefined__';

      /** Used to compose bitmasks for value comparisons. */
      var COMPARE_PARTIAL_FLAG = 1,
          COMPARE_UNORDERED_FLAG = 2;

      /** Used as references for various `Number` constants. */
      var MAX_SAFE_INTEGER = 9007199254740991;

      /** `Object#toString` result references. */
      var argsTag = '[object Arguments]',
          arrayTag = '[object Array]',
          asyncTag = '[object AsyncFunction]',
          boolTag = '[object Boolean]',
          dateTag = '[object Date]',
          errorTag = '[object Error]',
          funcTag = '[object Function]',
          genTag = '[object GeneratorFunction]',
          mapTag = '[object Map]',
          numberTag = '[object Number]',
          nullTag = '[object Null]',
          objectTag = '[object Object]',
          promiseTag = '[object Promise]',
          proxyTag = '[object Proxy]',
          regexpTag = '[object RegExp]',
          setTag = '[object Set]',
          stringTag = '[object String]',
          symbolTag = '[object Symbol]',
          undefinedTag = '[object Undefined]',
          weakMapTag = '[object WeakMap]';

      var arrayBufferTag = '[object ArrayBuffer]',
          dataViewTag = '[object DataView]',
          float32Tag = '[object Float32Array]',
          float64Tag = '[object Float64Array]',
          int8Tag = '[object Int8Array]',
          int16Tag = '[object Int16Array]',
          int32Tag = '[object Int32Array]',
          uint8Tag = '[object Uint8Array]',
          uint8ClampedTag = '[object Uint8ClampedArray]',
          uint16Tag = '[object Uint16Array]',
          uint32Tag = '[object Uint32Array]';

      /**
       * Used to match `RegExp`
       * [syntax characters](http://ecma-international.org/ecma-262/7.0/#sec-patterns).
       */
      var reRegExpChar = /[\\^$.*+?()[\]{}|]/g;

      /** Used to detect host constructors (Safari). */
      var reIsHostCtor = /^\[object .+?Constructor\]$/;

      /** Used to detect unsigned integer values. */
      var reIsUint = /^(?:0|[1-9]\d*)$/;

      /** Used to identify `toStringTag` values of typed arrays. */
      var typedArrayTags = {};
      typedArrayTags[float32Tag] = typedArrayTags[float64Tag] = typedArrayTags[int8Tag] = typedArrayTags[int16Tag] = typedArrayTags[int32Tag] = typedArrayTags[uint8Tag] = typedArrayTags[uint8ClampedTag] = typedArrayTags[uint16Tag] = typedArrayTags[uint32Tag] = true;
      typedArrayTags[argsTag] = typedArrayTags[arrayTag] = typedArrayTags[arrayBufferTag] = typedArrayTags[boolTag] = typedArrayTags[dataViewTag] = typedArrayTags[dateTag] = typedArrayTags[errorTag] = typedArrayTags[funcTag] = typedArrayTags[mapTag] = typedArrayTags[numberTag] = typedArrayTags[objectTag] = typedArrayTags[regexpTag] = typedArrayTags[setTag] = typedArrayTags[stringTag] = typedArrayTags[weakMapTag] = false;

      /** Detect free variable `global` from Node.js. */
      var freeGlobal = (typeof global === "undefined" ? "undefined" : _typeof(global)) == 'object' && global && global.Object === Object && global;

      /** Detect free variable `self`. */
      var freeSelf = (typeof self === "undefined" ? "undefined" : _typeof(self)) == 'object' && self && self.Object === Object && self;

      /** Used as a reference to the global object. */
      var root = freeGlobal || freeSelf || Function('return this')();

      /** Detect free variable `exports`. */
      var freeExports = (typeof exports === "undefined" ? "undefined" : _typeof(exports)) == 'object' && exports && !exports.nodeType && exports;

      /** Detect free variable `module`. */
      var freeModule = freeExports && (typeof module === "undefined" ? "undefined" : _typeof(module)) == 'object' && module && !module.nodeType && module;

      /** Detect the popular CommonJS extension `module.exports`. */
      var moduleExports = freeModule && freeModule.exports === freeExports;

      /** Detect free variable `process` from Node.js. */
      var freeProcess = moduleExports && freeGlobal.process;

      /** Used to access faster Node.js helpers. */
      var nodeUtil = function () {
        try {
          return freeProcess && freeProcess.binding && freeProcess.binding('util');
        } catch (e) {}
      }();

      /* Node.js helper references. */
      var nodeIsTypedArray = nodeUtil && nodeUtil.isTypedArray;

      /**
       * A specialized version of `_.filter` for arrays without support for
       * iteratee shorthands.
       *
       * @private
       * @param {Array} [array] The array to iterate over.
       * @param {Function} predicate The function invoked per iteration.
       * @returns {Array} Returns the new filtered array.
       */
      function arrayFilter(array, predicate) {
        var index = -1,
            length = array == null ? 0 : array.length,
            resIndex = 0,
            result = [];

        while (++index < length) {
          var value = array[index];
          if (predicate(value, index, array)) {
            result[resIndex++] = value;
          }
        }
        return result;
      }

      /**
       * Appends the elements of `values` to `array`.
       *
       * @private
       * @param {Array} array The array to modify.
       * @param {Array} values The values to append.
       * @returns {Array} Returns `array`.
       */
      function arrayPush(array, values) {
        var index = -1,
            length = values.length,
            offset = array.length;

        while (++index < length) {
          array[offset + index] = values[index];
        }
        return array;
      }

      /**
       * A specialized version of `_.some` for arrays without support for iteratee
       * shorthands.
       *
       * @private
       * @param {Array} [array] The array to iterate over.
       * @param {Function} predicate The function invoked per iteration.
       * @returns {boolean} Returns `true` if any element passes the predicate check,
       *  else `false`.
       */
      function arraySome(array, predicate) {
        var index = -1,
            length = array == null ? 0 : array.length;

        while (++index < length) {
          if (predicate(array[index], index, array)) {
            return true;
          }
        }
        return false;
      }

      /**
       * The base implementation of `_.times` without support for iteratee shorthands
       * or max array length checks.
       *
       * @private
       * @param {number} n The number of times to invoke `iteratee`.
       * @param {Function} iteratee The function invoked per iteration.
       * @returns {Array} Returns the array of results.
       */
      function baseTimes(n, iteratee) {
        var index = -1,
            result = Array(n);

        while (++index < n) {
          result[index] = iteratee(index);
        }
        return result;
      }

      /**
       * The base implementation of `_.unary` without support for storing metadata.
       *
       * @private
       * @param {Function} func The function to cap arguments for.
       * @returns {Function} Returns the new capped function.
       */
      function baseUnary(func) {
        return function (value) {
          return func(value);
        };
      }

      /**
       * Checks if a `cache` value for `key` exists.
       *
       * @private
       * @param {Object} cache The cache to query.
       * @param {string} key The key of the entry to check.
       * @returns {boolean} Returns `true` if an entry for `key` exists, else `false`.
       */
      function cacheHas(cache, key) {
        return cache.has(key);
      }

      /**
       * Gets the value at `key` of `object`.
       *
       * @private
       * @param {Object} [object] The object to query.
       * @param {string} key The key of the property to get.
       * @returns {*} Returns the property value.
       */
      function getValue(object, key) {
        return object == null ? undefined : object[key];
      }

      /**
       * Converts `map` to its key-value pairs.
       *
       * @private
       * @param {Object} map The map to convert.
       * @returns {Array} Returns the key-value pairs.
       */
      function mapToArray(map) {
        var index = -1,
            result = Array(map.size);

        map.forEach(function (value, key) {
          result[++index] = [key, value];
        });
        return result;
      }

      /**
       * Creates a unary function that invokes `func` with its argument transformed.
       *
       * @private
       * @param {Function} func The function to wrap.
       * @param {Function} transform The argument transform.
       * @returns {Function} Returns the new function.
       */
      function overArg(func, transform) {
        return function (arg) {
          return func(transform(arg));
        };
      }

      /**
       * Converts `set` to an array of its values.
       *
       * @private
       * @param {Object} set The set to convert.
       * @returns {Array} Returns the values.
       */
      function setToArray(set) {
        var index = -1,
            result = Array(set.size);

        set.forEach(function (value) {
          result[++index] = value;
        });
        return result;
      }

      /** Used for built-in method references. */
      var arrayProto = Array.prototype,
          funcProto = Function.prototype,
          objectProto = Object.prototype;

      /** Used to detect overreaching core-js shims. */
      var coreJsData = root['__core-js_shared__'];

      /** Used to resolve the decompiled source of functions. */
      var funcToString = funcProto.toString;

      /** Used to check objects for own properties. */
      var hasOwnProperty = objectProto.hasOwnProperty;

      /** Used to detect methods masquerading as native. */
      var maskSrcKey = function () {
        var uid = /[^.]+$/.exec(coreJsData && coreJsData.keys && coreJsData.keys.IE_PROTO || '');
        return uid ? 'Symbol(src)_1.' + uid : '';
      }();

      /**
       * Used to resolve the
       * [`toStringTag`](http://ecma-international.org/ecma-262/7.0/#sec-object.prototype.tostring)
       * of values.
       */
      var nativeObjectToString = objectProto.toString;

      /** Used to detect if a method is native. */
      var reIsNative = RegExp('^' + funcToString.call(hasOwnProperty).replace(reRegExpChar, '\\$&').replace(/hasOwnProperty|(function).*?(?=\\\()| for .+?(?=\\\])/g, '$1.*?') + '$');

      /** Built-in value references. */
      var Buffer = moduleExports ? root.Buffer : undefined,
          _Symbol2 = root.Symbol,
          Uint8Array = root.Uint8Array,
          propertyIsEnumerable = objectProto.propertyIsEnumerable,
          splice = arrayProto.splice,
          symToStringTag = _Symbol2 ? _Symbol2.toStringTag : undefined;

      /* Built-in method references for those with the same name as other `lodash` methods. */
      var nativeGetSymbols = Object.getOwnPropertySymbols,
          nativeIsBuffer = Buffer ? Buffer.isBuffer : undefined,
          nativeKeys = overArg(Object.keys, Object);

      /* Built-in method references that are verified to be native. */
      var DataView = getNative(root, 'DataView'),
          Map = getNative(root, 'Map'),
          Promise = getNative(root, 'Promise'),
          Set = getNative(root, 'Set'),
          WeakMap = getNative(root, 'WeakMap'),
          nativeCreate = getNative(Object, 'create');

      /** Used to detect maps, sets, and weakmaps. */
      var dataViewCtorString = toSource(DataView),
          mapCtorString = toSource(Map),
          promiseCtorString = toSource(Promise),
          setCtorString = toSource(Set),
          weakMapCtorString = toSource(WeakMap);

      /** Used to convert symbols to primitives and strings. */
      var symbolProto = _Symbol2 ? _Symbol2.prototype : undefined,
          symbolValueOf = symbolProto ? symbolProto.valueOf : undefined;

      /**
       * Creates a hash object.
       *
       * @private
       * @constructor
       * @param {Array} [entries] The key-value pairs to cache.
       */
      function Hash(entries) {
        var index = -1,
            length = entries == null ? 0 : entries.length;

        this.clear();
        while (++index < length) {
          var entry = entries[index];
          this.set(entry[0], entry[1]);
        }
      }

      /**
       * Removes all key-value entries from the hash.
       *
       * @private
       * @name clear
       * @memberOf Hash
       */
      function hashClear() {
        this.__data__ = nativeCreate ? nativeCreate(null) : {};
        this.size = 0;
      }

      /**
       * Removes `key` and its value from the hash.
       *
       * @private
       * @name delete
       * @memberOf Hash
       * @param {Object} hash The hash to modify.
       * @param {string} key The key of the value to remove.
       * @returns {boolean} Returns `true` if the entry was removed, else `false`.
       */
      function hashDelete(key) {
        var result = this.has(key) && delete this.__data__[key];
        this.size -= result ? 1 : 0;
        return result;
      }

      /**
       * Gets the hash value for `key`.
       *
       * @private
       * @name get
       * @memberOf Hash
       * @param {string} key The key of the value to get.
       * @returns {*} Returns the entry value.
       */
      function hashGet(key) {
        var data = this.__data__;
        if (nativeCreate) {
          var result = data[key];
          return result === HASH_UNDEFINED ? undefined : result;
        }
        return hasOwnProperty.call(data, key) ? data[key] : undefined;
      }

      /**
       * Checks if a hash value for `key` exists.
       *
       * @private
       * @name has
       * @memberOf Hash
       * @param {string} key The key of the entry to check.
       * @returns {boolean} Returns `true` if an entry for `key` exists, else `false`.
       */
      function hashHas(key) {
        var data = this.__data__;
        return nativeCreate ? data[key] !== undefined : hasOwnProperty.call(data, key);
      }

      /**
       * Sets the hash `key` to `value`.
       *
       * @private
       * @name set
       * @memberOf Hash
       * @param {string} key The key of the value to set.
       * @param {*} value The value to set.
       * @returns {Object} Returns the hash instance.
       */
      function hashSet(key, value) {
        var data = this.__data__;
        this.size += this.has(key) ? 0 : 1;
        data[key] = nativeCreate && value === undefined ? HASH_UNDEFINED : value;
        return this;
      }

      // Add methods to `Hash`.
      Hash.prototype.clear = hashClear;
      Hash.prototype['delete'] = hashDelete;
      Hash.prototype.get = hashGet;
      Hash.prototype.has = hashHas;
      Hash.prototype.set = hashSet;

      /**
       * Creates an list cache object.
       *
       * @private
       * @constructor
       * @param {Array} [entries] The key-value pairs to cache.
       */
      function ListCache(entries) {
        var index = -1,
            length = entries == null ? 0 : entries.length;

        this.clear();
        while (++index < length) {
          var entry = entries[index];
          this.set(entry[0], entry[1]);
        }
      }

      /**
       * Removes all key-value entries from the list cache.
       *
       * @private
       * @name clear
       * @memberOf ListCache
       */
      function listCacheClear() {
        this.__data__ = [];
        this.size = 0;
      }

      /**
       * Removes `key` and its value from the list cache.
       *
       * @private
       * @name delete
       * @memberOf ListCache
       * @param {string} key The key of the value to remove.
       * @returns {boolean} Returns `true` if the entry was removed, else `false`.
       */
      function listCacheDelete(key) {
        var data = this.__data__,
            index = assocIndexOf(data, key);

        if (index < 0) {
          return false;
        }
        var lastIndex = data.length - 1;
        if (index == lastIndex) {
          data.pop();
        } else {
          splice.call(data, index, 1);
        }
        --this.size;
        return true;
      }

      /**
       * Gets the list cache value for `key`.
       *
       * @private
       * @name get
       * @memberOf ListCache
       * @param {string} key The key of the value to get.
       * @returns {*} Returns the entry value.
       */
      function listCacheGet(key) {
        var data = this.__data__,
            index = assocIndexOf(data, key);

        return index < 0 ? undefined : data[index][1];
      }

      /**
       * Checks if a list cache value for `key` exists.
       *
       * @private
       * @name has
       * @memberOf ListCache
       * @param {string} key The key of the entry to check.
       * @returns {boolean} Returns `true` if an entry for `key` exists, else `false`.
       */
      function listCacheHas(key) {
        return assocIndexOf(this.__data__, key) > -1;
      }

      /**
       * Sets the list cache `key` to `value`.
       *
       * @private
       * @name set
       * @memberOf ListCache
       * @param {string} key The key of the value to set.
       * @param {*} value The value to set.
       * @returns {Object} Returns the list cache instance.
       */
      function listCacheSet(key, value) {
        var data = this.__data__,
            index = assocIndexOf(data, key);

        if (index < 0) {
          ++this.size;
          data.push([key, value]);
        } else {
          data[index][1] = value;
        }
        return this;
      }

      // Add methods to `ListCache`.
      ListCache.prototype.clear = listCacheClear;
      ListCache.prototype['delete'] = listCacheDelete;
      ListCache.prototype.get = listCacheGet;
      ListCache.prototype.has = listCacheHas;
      ListCache.prototype.set = listCacheSet;

      /**
       * Creates a map cache object to store key-value pairs.
       *
       * @private
       * @constructor
       * @param {Array} [entries] The key-value pairs to cache.
       */
      function MapCache(entries) {
        var index = -1,
            length = entries == null ? 0 : entries.length;

        this.clear();
        while (++index < length) {
          var entry = entries[index];
          this.set(entry[0], entry[1]);
        }
      }

      /**
       * Removes all key-value entries from the map.
       *
       * @private
       * @name clear
       * @memberOf MapCache
       */
      function mapCacheClear() {
        this.size = 0;
        this.__data__ = {
          'hash': new Hash(),
          'map': new (Map || ListCache)(),
          'string': new Hash()
        };
      }

      /**
       * Removes `key` and its value from the map.
       *
       * @private
       * @name delete
       * @memberOf MapCache
       * @param {string} key The key of the value to remove.
       * @returns {boolean} Returns `true` if the entry was removed, else `false`.
       */
      function mapCacheDelete(key) {
        var result = getMapData(this, key)['delete'](key);
        this.size -= result ? 1 : 0;
        return result;
      }

      /**
       * Gets the map value for `key`.
       *
       * @private
       * @name get
       * @memberOf MapCache
       * @param {string} key The key of the value to get.
       * @returns {*} Returns the entry value.
       */
      function mapCacheGet(key) {
        return getMapData(this, key).get(key);
      }

      /**
       * Checks if a map value for `key` exists.
       *
       * @private
       * @name has
       * @memberOf MapCache
       * @param {string} key The key of the entry to check.
       * @returns {boolean} Returns `true` if an entry for `key` exists, else `false`.
       */
      function mapCacheHas(key) {
        return getMapData(this, key).has(key);
      }

      /**
       * Sets the map `key` to `value`.
       *
       * @private
       * @name set
       * @memberOf MapCache
       * @param {string} key The key of the value to set.
       * @param {*} value The value to set.
       * @returns {Object} Returns the map cache instance.
       */
      function mapCacheSet(key, value) {
        var data = getMapData(this, key),
            size = data.size;

        data.set(key, value);
        this.size += data.size == size ? 0 : 1;
        return this;
      }

      // Add methods to `MapCache`.
      MapCache.prototype.clear = mapCacheClear;
      MapCache.prototype['delete'] = mapCacheDelete;
      MapCache.prototype.get = mapCacheGet;
      MapCache.prototype.has = mapCacheHas;
      MapCache.prototype.set = mapCacheSet;

      /**
       *
       * Creates an array cache object to store unique values.
       *
       * @private
       * @constructor
       * @param {Array} [values] The values to cache.
       */
      function SetCache(values) {
        var index = -1,
            length = values == null ? 0 : values.length;

        this.__data__ = new MapCache();
        while (++index < length) {
          this.add(values[index]);
        }
      }

      /**
       * Adds `value` to the array cache.
       *
       * @private
       * @name add
       * @memberOf SetCache
       * @alias push
       * @param {*} value The value to cache.
       * @returns {Object} Returns the cache instance.
       */
      function setCacheAdd(value) {
        this.__data__.set(value, HASH_UNDEFINED);
        return this;
      }

      /**
       * Checks if `value` is in the array cache.
       *
       * @private
       * @name has
       * @memberOf SetCache
       * @param {*} value The value to search for.
       * @returns {number} Returns `true` if `value` is found, else `false`.
       */
      function setCacheHas(value) {
        return this.__data__.has(value);
      }

      // Add methods to `SetCache`.
      SetCache.prototype.add = SetCache.prototype.push = setCacheAdd;
      SetCache.prototype.has = setCacheHas;

      /**
       * Creates a stack cache object to store key-value pairs.
       *
       * @private
       * @constructor
       * @param {Array} [entries] The key-value pairs to cache.
       */
      function Stack(entries) {
        var data = this.__data__ = new ListCache(entries);
        this.size = data.size;
      }

      /**
       * Removes all key-value entries from the stack.
       *
       * @private
       * @name clear
       * @memberOf Stack
       */
      function stackClear() {
        this.__data__ = new ListCache();
        this.size = 0;
      }

      /**
       * Removes `key` and its value from the stack.
       *
       * @private
       * @name delete
       * @memberOf Stack
       * @param {string} key The key of the value to remove.
       * @returns {boolean} Returns `true` if the entry was removed, else `false`.
       */
      function stackDelete(key) {
        var data = this.__data__,
            result = data['delete'](key);

        this.size = data.size;
        return result;
      }

      /**
       * Gets the stack value for `key`.
       *
       * @private
       * @name get
       * @memberOf Stack
       * @param {string} key The key of the value to get.
       * @returns {*} Returns the entry value.
       */
      function stackGet(key) {
        return this.__data__.get(key);
      }

      /**
       * Checks if a stack value for `key` exists.
       *
       * @private
       * @name has
       * @memberOf Stack
       * @param {string} key The key of the entry to check.
       * @returns {boolean} Returns `true` if an entry for `key` exists, else `false`.
       */
      function stackHas(key) {
        return this.__data__.has(key);
      }

      /**
       * Sets the stack `key` to `value`.
       *
       * @private
       * @name set
       * @memberOf Stack
       * @param {string} key The key of the value to set.
       * @param {*} value The value to set.
       * @returns {Object} Returns the stack cache instance.
       */
      function stackSet(key, value) {
        var data = this.__data__;
        if (data instanceof ListCache) {
          var pairs = data.__data__;
          if (!Map || pairs.length < LARGE_ARRAY_SIZE - 1) {
            pairs.push([key, value]);
            this.size = ++data.size;
            return this;
          }
          data = this.__data__ = new MapCache(pairs);
        }
        data.set(key, value);
        this.size = data.size;
        return this;
      }

      // Add methods to `Stack`.
      Stack.prototype.clear = stackClear;
      Stack.prototype['delete'] = stackDelete;
      Stack.prototype.get = stackGet;
      Stack.prototype.has = stackHas;
      Stack.prototype.set = stackSet;

      /**
       * Creates an array of the enumerable property names of the array-like `value`.
       *
       * @private
       * @param {*} value The value to query.
       * @param {boolean} inherited Specify returning inherited property names.
       * @returns {Array} Returns the array of property names.
       */
      function arrayLikeKeys(value, inherited) {
        var isArr = isArray(value),
            isArg = !isArr && isArguments(value),
            isBuff = !isArr && !isArg && isBuffer(value),
            isType = !isArr && !isArg && !isBuff && isTypedArray(value),
            skipIndexes = isArr || isArg || isBuff || isType,
            result = skipIndexes ? baseTimes(value.length, String) : [],
            length = result.length;

        for (var key in value) {
          if ((inherited || hasOwnProperty.call(value, key)) && !(skipIndexes && (
          // Safari 9 has enumerable `arguments.length` in strict mode.
          key == 'length' ||
          // Node.js 0.10 has enumerable non-index properties on buffers.
          isBuff && (key == 'offset' || key == 'parent') ||
          // PhantomJS 2 has enumerable non-index properties on typed arrays.
          isType && (key == 'buffer' || key == 'byteLength' || key == 'byteOffset') ||
          // Skip index properties.
          isIndex(key, length)))) {
            result.push(key);
          }
        }
        return result;
      }

      /**
       * Gets the index at which the `key` is found in `array` of key-value pairs.
       *
       * @private
       * @param {Array} array The array to inspect.
       * @param {*} key The key to search for.
       * @returns {number} Returns the index of the matched value, else `-1`.
       */
      function assocIndexOf(array, key) {
        var length = array.length;
        while (length--) {
          if (eq(array[length][0], key)) {
            return length;
          }
        }
        return -1;
      }

      /**
       * The base implementation of `getAllKeys` and `getAllKeysIn` which uses
       * `keysFunc` and `symbolsFunc` to get the enumerable property names and
       * symbols of `object`.
       *
       * @private
       * @param {Object} object The object to query.
       * @param {Function} keysFunc The function to get the keys of `object`.
       * @param {Function} symbolsFunc The function to get the symbols of `object`.
       * @returns {Array} Returns the array of property names and symbols.
       */
      function baseGetAllKeys(object, keysFunc, symbolsFunc) {
        var result = keysFunc(object);
        return isArray(object) ? result : arrayPush(result, symbolsFunc(object));
      }

      /**
       * The base implementation of `getTag` without fallbacks for buggy environments.
       *
       * @private
       * @param {*} value The value to query.
       * @returns {string} Returns the `toStringTag`.
       */
      function baseGetTag(value) {
        if (value == null) {
          return value === undefined ? undefinedTag : nullTag;
        }
        return symToStringTag && symToStringTag in Object(value) ? getRawTag(value) : objectToString(value);
      }

      /**
       * The base implementation of `_.isArguments`.
       *
       * @private
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is an `arguments` object,
       */
      function baseIsArguments(value) {
        return isObjectLike(value) && baseGetTag(value) == argsTag;
      }

      /**
       * The base implementation of `_.isEqual` which supports partial comparisons
       * and tracks traversed objects.
       *
       * @private
       * @param {*} value The value to compare.
       * @param {*} other The other value to compare.
       * @param {boolean} bitmask The bitmask flags.
       *  1 - Unordered comparison
       *  2 - Partial comparison
       * @param {Function} [customizer] The function to customize comparisons.
       * @param {Object} [stack] Tracks traversed `value` and `other` objects.
       * @returns {boolean} Returns `true` if the values are equivalent, else `false`.
       */
      function baseIsEqual(value, other, bitmask, customizer, stack) {
        if (value === other) {
          return true;
        }
        if (value == null || other == null || !isObjectLike(value) && !isObjectLike(other)) {
          return value !== value && other !== other;
        }
        return baseIsEqualDeep(value, other, bitmask, customizer, baseIsEqual, stack);
      }

      /**
       * A specialized version of `baseIsEqual` for arrays and objects which performs
       * deep comparisons and tracks traversed objects enabling objects with circular
       * references to be compared.
       *
       * @private
       * @param {Object} object The object to compare.
       * @param {Object} other The other object to compare.
       * @param {number} bitmask The bitmask flags. See `baseIsEqual` for more details.
       * @param {Function} customizer The function to customize comparisons.
       * @param {Function} equalFunc The function to determine equivalents of values.
       * @param {Object} [stack] Tracks traversed `object` and `other` objects.
       * @returns {boolean} Returns `true` if the objects are equivalent, else `false`.
       */
      function baseIsEqualDeep(object, other, bitmask, customizer, equalFunc, stack) {
        var objIsArr = isArray(object),
            othIsArr = isArray(other),
            objTag = objIsArr ? arrayTag : getTag(object),
            othTag = othIsArr ? arrayTag : getTag(other);

        objTag = objTag == argsTag ? objectTag : objTag;
        othTag = othTag == argsTag ? objectTag : othTag;

        var objIsObj = objTag == objectTag,
            othIsObj = othTag == objectTag,
            isSameTag = objTag == othTag;

        if (isSameTag && isBuffer(object)) {
          if (!isBuffer(other)) {
            return false;
          }
          objIsArr = true;
          objIsObj = false;
        }
        if (isSameTag && !objIsObj) {
          stack || (stack = new Stack());
          return objIsArr || isTypedArray(object) ? equalArrays(object, other, bitmask, customizer, equalFunc, stack) : equalByTag(object, other, objTag, bitmask, customizer, equalFunc, stack);
        }
        if (!(bitmask & COMPARE_PARTIAL_FLAG)) {
          var objIsWrapped = objIsObj && hasOwnProperty.call(object, '__wrapped__'),
              othIsWrapped = othIsObj && hasOwnProperty.call(other, '__wrapped__');

          if (objIsWrapped || othIsWrapped) {
            var objUnwrapped = objIsWrapped ? object.value() : object,
                othUnwrapped = othIsWrapped ? other.value() : other;

            stack || (stack = new Stack());
            return equalFunc(objUnwrapped, othUnwrapped, bitmask, customizer, stack);
          }
        }
        if (!isSameTag) {
          return false;
        }
        stack || (stack = new Stack());
        return equalObjects(object, other, bitmask, customizer, equalFunc, stack);
      }

      /**
       * The base implementation of `_.isNative` without bad shim checks.
       *
       * @private
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a native function,
       *  else `false`.
       */
      function baseIsNative(value) {
        if (!isObject(value) || isMasked(value)) {
          return false;
        }
        var pattern = isFunction(value) ? reIsNative : reIsHostCtor;
        return pattern.test(toSource(value));
      }

      /**
       * The base implementation of `_.isTypedArray` without Node.js optimizations.
       *
       * @private
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a typed array, else `false`.
       */
      function baseIsTypedArray(value) {
        return isObjectLike(value) && isLength(value.length) && !!typedArrayTags[baseGetTag(value)];
      }

      /**
       * The base implementation of `_.keys` which doesn't treat sparse arrays as dense.
       *
       * @private
       * @param {Object} object The object to query.
       * @returns {Array} Returns the array of property names.
       */
      function baseKeys(object) {
        if (!isPrototype(object)) {
          return nativeKeys(object);
        }
        var result = [];
        for (var key in Object(object)) {
          if (hasOwnProperty.call(object, key) && key != 'constructor') {
            result.push(key);
          }
        }
        return result;
      }

      /**
       * A specialized version of `baseIsEqualDeep` for arrays with support for
       * partial deep comparisons.
       *
       * @private
       * @param {Array} array The array to compare.
       * @param {Array} other The other array to compare.
       * @param {number} bitmask The bitmask flags. See `baseIsEqual` for more details.
       * @param {Function} customizer The function to customize comparisons.
       * @param {Function} equalFunc The function to determine equivalents of values.
       * @param {Object} stack Tracks traversed `array` and `other` objects.
       * @returns {boolean} Returns `true` if the arrays are equivalent, else `false`.
       */
      function equalArrays(array, other, bitmask, customizer, equalFunc, stack) {
        var isPartial = bitmask & COMPARE_PARTIAL_FLAG,
            arrLength = array.length,
            othLength = other.length;

        if (arrLength != othLength && !(isPartial && othLength > arrLength)) {
          return false;
        }
        // Assume cyclic values are equal.
        var stacked = stack.get(array);
        if (stacked && stack.get(other)) {
          return stacked == other;
        }
        var index = -1,
            result = true,
            seen = bitmask & COMPARE_UNORDERED_FLAG ? new SetCache() : undefined;

        stack.set(array, other);
        stack.set(other, array);

        // Ignore non-index properties.
        while (++index < arrLength) {
          var arrValue = array[index],
              othValue = other[index];

          if (customizer) {
            var compared = isPartial ? customizer(othValue, arrValue, index, other, array, stack) : customizer(arrValue, othValue, index, array, other, stack);
          }
          if (compared !== undefined) {
            if (compared) {
              continue;
            }
            result = false;
            break;
          }
          // Recursively compare arrays (susceptible to call stack limits).
          if (seen) {
            if (!arraySome(other, function (othValue, othIndex) {
              if (!cacheHas(seen, othIndex) && (arrValue === othValue || equalFunc(arrValue, othValue, bitmask, customizer, stack))) {
                return seen.push(othIndex);
              }
            })) {
              result = false;
              break;
            }
          } else if (!(arrValue === othValue || equalFunc(arrValue, othValue, bitmask, customizer, stack))) {
            result = false;
            break;
          }
        }
        stack['delete'](array);
        stack['delete'](other);
        return result;
      }

      /**
       * A specialized version of `baseIsEqualDeep` for comparing objects of
       * the same `toStringTag`.
       *
       * **Note:** This function only supports comparing values with tags of
       * `Boolean`, `Date`, `Error`, `Number`, `RegExp`, or `String`.
       *
       * @private
       * @param {Object} object The object to compare.
       * @param {Object} other The other object to compare.
       * @param {string} tag The `toStringTag` of the objects to compare.
       * @param {number} bitmask The bitmask flags. See `baseIsEqual` for more details.
       * @param {Function} customizer The function to customize comparisons.
       * @param {Function} equalFunc The function to determine equivalents of values.
       * @param {Object} stack Tracks traversed `object` and `other` objects.
       * @returns {boolean} Returns `true` if the objects are equivalent, else `false`.
       */
      function equalByTag(object, other, tag, bitmask, customizer, equalFunc, stack) {
        switch (tag) {
          case dataViewTag:
            if (object.byteLength != other.byteLength || object.byteOffset != other.byteOffset) {
              return false;
            }
            object = object.buffer;
            other = other.buffer;

          case arrayBufferTag:
            if (object.byteLength != other.byteLength || !equalFunc(new Uint8Array(object), new Uint8Array(other))) {
              return false;
            }
            return true;

          case boolTag:
          case dateTag:
          case numberTag:
            // Coerce booleans to `1` or `0` and dates to milliseconds.
            // Invalid dates are coerced to `NaN`.
            return eq(+object, +other);

          case errorTag:
            return object.name == other.name && object.message == other.message;

          case regexpTag:
          case stringTag:
            // Coerce regexes to strings and treat strings, primitives and objects,
            // as equal. See http://www.ecma-international.org/ecma-262/7.0/#sec-regexp.prototype.tostring
            // for more details.
            return object == other + '';

          case mapTag:
            var convert = mapToArray;

          case setTag:
            var isPartial = bitmask & COMPARE_PARTIAL_FLAG;
            convert || (convert = setToArray);

            if (object.size != other.size && !isPartial) {
              return false;
            }
            // Assume cyclic values are equal.
            var stacked = stack.get(object);
            if (stacked) {
              return stacked == other;
            }
            bitmask |= COMPARE_UNORDERED_FLAG;

            // Recursively compare objects (susceptible to call stack limits).
            stack.set(object, other);
            var result = equalArrays(convert(object), convert(other), bitmask, customizer, equalFunc, stack);
            stack['delete'](object);
            return result;

          case symbolTag:
            if (symbolValueOf) {
              return symbolValueOf.call(object) == symbolValueOf.call(other);
            }
        }
        return false;
      }

      /**
       * A specialized version of `baseIsEqualDeep` for objects with support for
       * partial deep comparisons.
       *
       * @private
       * @param {Object} object The object to compare.
       * @param {Object} other The other object to compare.
       * @param {number} bitmask The bitmask flags. See `baseIsEqual` for more details.
       * @param {Function} customizer The function to customize comparisons.
       * @param {Function} equalFunc The function to determine equivalents of values.
       * @param {Object} stack Tracks traversed `object` and `other` objects.
       * @returns {boolean} Returns `true` if the objects are equivalent, else `false`.
       */
      function equalObjects(object, other, bitmask, customizer, equalFunc, stack) {
        var isPartial = bitmask & COMPARE_PARTIAL_FLAG,
            objProps = getAllKeys(object),
            objLength = objProps.length,
            othProps = getAllKeys(other),
            othLength = othProps.length;

        if (objLength != othLength && !isPartial) {
          return false;
        }
        var index = objLength;
        while (index--) {
          var key = objProps[index];
          if (!(isPartial ? key in other : hasOwnProperty.call(other, key))) {
            return false;
          }
        }
        // Assume cyclic values are equal.
        var stacked = stack.get(object);
        if (stacked && stack.get(other)) {
          return stacked == other;
        }
        var result = true;
        stack.set(object, other);
        stack.set(other, object);

        var skipCtor = isPartial;
        while (++index < objLength) {
          key = objProps[index];
          var objValue = object[key],
              othValue = other[key];

          if (customizer) {
            var compared = isPartial ? customizer(othValue, objValue, key, other, object, stack) : customizer(objValue, othValue, key, object, other, stack);
          }
          // Recursively compare objects (susceptible to call stack limits).
          if (!(compared === undefined ? objValue === othValue || equalFunc(objValue, othValue, bitmask, customizer, stack) : compared)) {
            result = false;
            break;
          }
          skipCtor || (skipCtor = key == 'constructor');
        }
        if (result && !skipCtor) {
          var objCtor = object.constructor,
              othCtor = other.constructor;

          // Non `Object` object instances with different constructors are not equal.
          if (objCtor != othCtor && 'constructor' in object && 'constructor' in other && !(typeof objCtor == 'function' && objCtor instanceof objCtor && typeof othCtor == 'function' && othCtor instanceof othCtor)) {
            result = false;
          }
        }
        stack['delete'](object);
        stack['delete'](other);
        return result;
      }

      /**
       * Creates an array of own enumerable property names and symbols of `object`.
       *
       * @private
       * @param {Object} object The object to query.
       * @returns {Array} Returns the array of property names and symbols.
       */
      function getAllKeys(object) {
        return baseGetAllKeys(object, keys, getSymbols);
      }

      /**
       * Gets the data for `map`.
       *
       * @private
       * @param {Object} map The map to query.
       * @param {string} key The reference key.
       * @returns {*} Returns the map data.
       */
      function getMapData(map, key) {
        var data = map.__data__;
        return isKeyable(key) ? data[typeof key == 'string' ? 'string' : 'hash'] : data.map;
      }

      /**
       * Gets the native function at `key` of `object`.
       *
       * @private
       * @param {Object} object The object to query.
       * @param {string} key The key of the method to get.
       * @returns {*} Returns the function if it's native, else `undefined`.
       */
      function getNative(object, key) {
        var value = getValue(object, key);
        return baseIsNative(value) ? value : undefined;
      }

      /**
       * A specialized version of `baseGetTag` which ignores `Symbol.toStringTag` values.
       *
       * @private
       * @param {*} value The value to query.
       * @returns {string} Returns the raw `toStringTag`.
       */
      function getRawTag(value) {
        var isOwn = hasOwnProperty.call(value, symToStringTag),
            tag = value[symToStringTag];

        try {
          value[symToStringTag] = undefined;
          var unmasked = true;
        } catch (e) {}

        var result = nativeObjectToString.call(value);
        if (unmasked) {
          if (isOwn) {
            value[symToStringTag] = tag;
          } else {
            delete value[symToStringTag];
          }
        }
        return result;
      }

      /**
       * Creates an array of the own enumerable symbols of `object`.
       *
       * @private
       * @param {Object} object The object to query.
       * @returns {Array} Returns the array of symbols.
       */
      var getSymbols = !nativeGetSymbols ? stubArray : function (object) {
        if (object == null) {
          return [];
        }
        object = Object(object);
        return arrayFilter(nativeGetSymbols(object), function (symbol) {
          return propertyIsEnumerable.call(object, symbol);
        });
      };

      /**
       * Gets the `toStringTag` of `value`.
       *
       * @private
       * @param {*} value The value to query.
       * @returns {string} Returns the `toStringTag`.
       */
      var getTag = baseGetTag;

      // Fallback for data views, maps, sets, and weak maps in IE 11 and promises in Node.js < 6.
      if (DataView && getTag(new DataView(new ArrayBuffer(1))) != dataViewTag || Map && getTag(new Map()) != mapTag || Promise && getTag(Promise.resolve()) != promiseTag || Set && getTag(new Set()) != setTag || WeakMap && getTag(new WeakMap()) != weakMapTag) {
        getTag = function getTag(value) {
          var result = baseGetTag(value),
              Ctor = result == objectTag ? value.constructor : undefined,
              ctorString = Ctor ? toSource(Ctor) : '';

          if (ctorString) {
            switch (ctorString) {
              case dataViewCtorString:
                return dataViewTag;
              case mapCtorString:
                return mapTag;
              case promiseCtorString:
                return promiseTag;
              case setCtorString:
                return setTag;
              case weakMapCtorString:
                return weakMapTag;
            }
          }
          return result;
        };
      }

      /**
       * Checks if `value` is a valid array-like index.
       *
       * @private
       * @param {*} value The value to check.
       * @param {number} [length=MAX_SAFE_INTEGER] The upper bounds of a valid index.
       * @returns {boolean} Returns `true` if `value` is a valid index, else `false`.
       */
      function isIndex(value, length) {
        length = length == null ? MAX_SAFE_INTEGER : length;
        return !!length && (typeof value == 'number' || reIsUint.test(value)) && value > -1 && value % 1 == 0 && value < length;
      }

      /**
       * Checks if `value` is suitable for use as unique object key.
       *
       * @private
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is suitable, else `false`.
       */
      function isKeyable(value) {
        var type = typeof value === "undefined" ? "undefined" : _typeof(value);
        return type == 'string' || type == 'number' || type == 'symbol' || type == 'boolean' ? value !== '__proto__' : value === null;
      }

      /**
       * Checks if `func` has its source masked.
       *
       * @private
       * @param {Function} func The function to check.
       * @returns {boolean} Returns `true` if `func` is masked, else `false`.
       */
      function isMasked(func) {
        return !!maskSrcKey && maskSrcKey in func;
      }

      /**
       * Checks if `value` is likely a prototype object.
       *
       * @private
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a prototype, else `false`.
       */
      function isPrototype(value) {
        var Ctor = value && value.constructor,
            proto = typeof Ctor == 'function' && Ctor.prototype || objectProto;

        return value === proto;
      }

      /**
       * Converts `value` to a string using `Object.prototype.toString`.
       *
       * @private
       * @param {*} value The value to convert.
       * @returns {string} Returns the converted string.
       */
      function objectToString(value) {
        return nativeObjectToString.call(value);
      }

      /**
       * Converts `func` to its source code.
       *
       * @private
       * @param {Function} func The function to convert.
       * @returns {string} Returns the source code.
       */
      function toSource(func) {
        if (func != null) {
          try {
            return funcToString.call(func);
          } catch (e) {}
          try {
            return func + '';
          } catch (e) {}
        }
        return '';
      }

      /**
       * Performs a
       * [`SameValueZero`](http://ecma-international.org/ecma-262/7.0/#sec-samevaluezero)
       * comparison between two values to determine if they are equivalent.
       *
       * @static
       * @memberOf _
       * @since 4.0.0
       * @category Lang
       * @param {*} value The value to compare.
       * @param {*} other The other value to compare.
       * @returns {boolean} Returns `true` if the values are equivalent, else `false`.
       * @example
       *
       * var object = { 'a': 1 };
       * var other = { 'a': 1 };
       *
       * _.eq(object, object);
       * // => true
       *
       * _.eq(object, other);
       * // => false
       *
       * _.eq('a', 'a');
       * // => true
       *
       * _.eq('a', Object('a'));
       * // => false
       *
       * _.eq(NaN, NaN);
       * // => true
       */
      function eq(value, other) {
        return value === other || value !== value && other !== other;
      }

      /**
       * Checks if `value` is likely an `arguments` object.
       *
       * @static
       * @memberOf _
       * @since 0.1.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is an `arguments` object,
       *  else `false`.
       * @example
       *
       * _.isArguments(function() { return arguments; }());
       * // => true
       *
       * _.isArguments([1, 2, 3]);
       * // => false
       */
      var isArguments = baseIsArguments(function () {
        return arguments;
      }()) ? baseIsArguments : function (value) {
        return isObjectLike(value) && hasOwnProperty.call(value, 'callee') && !propertyIsEnumerable.call(value, 'callee');
      };

      /**
       * Checks if `value` is classified as an `Array` object.
       *
       * @static
       * @memberOf _
       * @since 0.1.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is an array, else `false`.
       * @example
       *
       * _.isArray([1, 2, 3]);
       * // => true
       *
       * _.isArray(document.body.children);
       * // => false
       *
       * _.isArray('abc');
       * // => false
       *
       * _.isArray(_.noop);
       * // => false
       */
      var isArray = Array.isArray;

      /**
       * Checks if `value` is array-like. A value is considered array-like if it's
       * not a function and has a `value.length` that's an integer greater than or
       * equal to `0` and less than or equal to `Number.MAX_SAFE_INTEGER`.
       *
       * @static
       * @memberOf _
       * @since 4.0.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is array-like, else `false`.
       * @example
       *
       * _.isArrayLike([1, 2, 3]);
       * // => true
       *
       * _.isArrayLike(document.body.children);
       * // => true
       *
       * _.isArrayLike('abc');
       * // => true
       *
       * _.isArrayLike(_.noop);
       * // => false
       */
      function isArrayLike(value) {
        return value != null && isLength(value.length) && !isFunction(value);
      }

      /**
       * Checks if `value` is a buffer.
       *
       * @static
       * @memberOf _
       * @since 4.3.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a buffer, else `false`.
       * @example
       *
       * _.isBuffer(new Buffer(2));
       * // => true
       *
       * _.isBuffer(new Uint8Array(2));
       * // => false
       */
      var isBuffer = nativeIsBuffer || stubFalse;

      /**
       * Performs a deep comparison between two values to determine if they are
       * equivalent.
       *
       * **Note:** This method supports comparing arrays, array buffers, booleans,
       * date objects, error objects, maps, numbers, `Object` objects, regexes,
       * sets, strings, symbols, and typed arrays. `Object` objects are compared
       * by their own, not inherited, enumerable properties. Functions and DOM
       * nodes are compared by strict equality, i.e. `===`.
       *
       * @static
       * @memberOf _
       * @since 0.1.0
       * @category Lang
       * @param {*} value The value to compare.
       * @param {*} other The other value to compare.
       * @returns {boolean} Returns `true` if the values are equivalent, else `false`.
       * @example
       *
       * var object = { 'a': 1 };
       * var other = { 'a': 1 };
       *
       * _.isEqual(object, other);
       * // => true
       *
       * object === other;
       * // => false
       */
      function isEqual(value, other) {
        return baseIsEqual(value, other);
      }

      /**
       * Checks if `value` is classified as a `Function` object.
       *
       * @static
       * @memberOf _
       * @since 0.1.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a function, else `false`.
       * @example
       *
       * _.isFunction(_);
       * // => true
       *
       * _.isFunction(/abc/);
       * // => false
       */
      function isFunction(value) {
        if (!isObject(value)) {
          return false;
        }
        // The use of `Object#toString` avoids issues with the `typeof` operator
        // in Safari 9 which returns 'object' for typed arrays and other constructors.
        var tag = baseGetTag(value);
        return tag == funcTag || tag == genTag || tag == asyncTag || tag == proxyTag;
      }

      /**
       * Checks if `value` is a valid array-like length.
       *
       * **Note:** This method is loosely based on
       * [`ToLength`](http://ecma-international.org/ecma-262/7.0/#sec-tolength).
       *
       * @static
       * @memberOf _
       * @since 4.0.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a valid length, else `false`.
       * @example
       *
       * _.isLength(3);
       * // => true
       *
       * _.isLength(Number.MIN_VALUE);
       * // => false
       *
       * _.isLength(Infinity);
       * // => false
       *
       * _.isLength('3');
       * // => false
       */
      function isLength(value) {
        return typeof value == 'number' && value > -1 && value % 1 == 0 && value <= MAX_SAFE_INTEGER;
      }

      /**
       * Checks if `value` is the
       * [language type](http://www.ecma-international.org/ecma-262/7.0/#sec-ecmascript-language-types)
       * of `Object`. (e.g. arrays, functions, objects, regexes, `new Number(0)`, and `new String('')`)
       *
       * @static
       * @memberOf _
       * @since 0.1.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is an object, else `false`.
       * @example
       *
       * _.isObject({});
       * // => true
       *
       * _.isObject([1, 2, 3]);
       * // => true
       *
       * _.isObject(_.noop);
       * // => true
       *
       * _.isObject(null);
       * // => false
       */
      function isObject(value) {
        var type = typeof value === "undefined" ? "undefined" : _typeof(value);
        return value != null && (type == 'object' || type == 'function');
      }

      /**
       * Checks if `value` is object-like. A value is object-like if it's not `null`
       * and has a `typeof` result of "object".
       *
       * @static
       * @memberOf _
       * @since 4.0.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is object-like, else `false`.
       * @example
       *
       * _.isObjectLike({});
       * // => true
       *
       * _.isObjectLike([1, 2, 3]);
       * // => true
       *
       * _.isObjectLike(_.noop);
       * // => false
       *
       * _.isObjectLike(null);
       * // => false
       */
      function isObjectLike(value) {
        return value != null && (typeof value === "undefined" ? "undefined" : _typeof(value)) == 'object';
      }

      /**
       * Checks if `value` is classified as a typed array.
       *
       * @static
       * @memberOf _
       * @since 3.0.0
       * @category Lang
       * @param {*} value The value to check.
       * @returns {boolean} Returns `true` if `value` is a typed array, else `false`.
       * @example
       *
       * _.isTypedArray(new Uint8Array);
       * // => true
       *
       * _.isTypedArray([]);
       * // => false
       */
      var isTypedArray = nodeIsTypedArray ? baseUnary(nodeIsTypedArray) : baseIsTypedArray;

      /**
       * Creates an array of the own enumerable property names of `object`.
       *
       * **Note:** Non-object values are coerced to objects. See the
       * [ES spec](http://ecma-international.org/ecma-262/7.0/#sec-object.keys)
       * for more details.
       *
       * @static
       * @since 0.1.0
       * @memberOf _
       * @category Object
       * @param {Object} object The object to query.
       * @returns {Array} Returns the array of property names.
       * @example
       *
       * function Foo() {
       *   this.a = 1;
       *   this.b = 2;
       * }
       *
       * Foo.prototype.c = 3;
       *
       * _.keys(new Foo);
       * // => ['a', 'b'] (iteration order is not guaranteed)
       *
       * _.keys('hi');
       * // => ['0', '1']
       */
      function keys(object) {
        return isArrayLike(object) ? arrayLikeKeys(object) : baseKeys(object);
      }

      /**
       * This method returns a new empty array.
       *
       * @static
       * @memberOf _
       * @since 4.13.0
       * @category Util
       * @returns {Array} Returns the new empty array.
       * @example
       *
       * var arrays = _.times(2, _.stubArray);
       *
       * console.log(arrays);
       * // => [[], []]
       *
       * console.log(arrays[0] === arrays[1]);
       * // => false
       */
      function stubArray() {
        return [];
      }

      /**
       * This method returns `false`.
       *
       * @static
       * @memberOf _
       * @since 4.13.0
       * @category Util
       * @returns {boolean} Returns `false`.
       * @example
       *
       * _.times(2, _.stubFalse);
       * // => [false, false]
       */
      function stubFalse() {
        return false;
      }

      module.exports = isEqual;
    }).call(this, typeof global !== "undefined" ? global : typeof self !== "undefined" ? self : typeof window !== "undefined" ? window : {});
  }, {}], 15: [function (require, module, exports) {
    window.requestAnimationFrame = window.requestAnimationFrame || window.webkitRequestAnimationFrame || window.mozRequestAnimationFrame;

    /**
     * Handles debouncing of events via requestAnimationFrame
     * @see http://www.html5rocks.com/en/tutorials/speed/animations/
     * @param {Function} callback The callback to handle whichever event
     */
    function Debouncer(callback) {
      this.callback = callback;
      this.ticking = false;
    }
    Debouncer.prototype = {
      constructor: Debouncer,

      /**
       * dispatches the event to the supplied callback
       * @private
       */
      update: function update() {
        this.callback && this.callback();
        this.ticking = false;
      },

      /**
       * ensures events don't get stacked
       * @private
       */
      requestTick: function requestTick() {
        if (!this.ticking) {
          requestAnimationFrame(this.rafCallback || (this.rafCallback = this.update.bind(this)));
          this.ticking = true;
        }
      },

      /**
       * Attach this as the event listeners
       */
      handleEvent: function handleEvent() {
        this.requestTick();
      }
    };

    module.exports = Debouncer;
  }, {}], 16: [function (require, module, exports) {
    var isEqual = require('lodash.isequal'),
        forEach = require('lodash.foreach'),
        isEmpty = require('lodash.isempty'),
        cloneObject = require('lodash.clone'),
        extendObject = require('lodash.assign'),
        debouncer = require("./Debouncer");

    function Scrllr(options) {
      options = extendObject(Scrllr.options, options);

      this.lastKnownScrollY = 0;
      this.initialised = false;
      this.onScrollCallback = options.onScrollCallback;
    }

    Scrllr.prototype = {
      constructor: Scrllr,

      init: function init() {
        this.debouncer = new debouncer(this.update.bind(this));

        // defer event registration to handle browser
        // potentially restoring previous scroll position
        setTimeout(this.attachEvent.bind(this), 100);

        return this;
      },

      attachEvent: function attachEvent() {
        if (!this.initialised) {
          this.lastKnownScrollY = this.getScrollY();
          this.initialised = true;

          window.addEventListener('scroll', this.debouncer, false);
          this.debouncer.handleEvent();
        }
      },

      getScrollY: function getScrollY() {
        return window.pageYOffset !== undefined ? window.pageYOffset : window.scrollTop !== undefined ? window.scrollTop : (document.documentElement || document.body.parentNode || document.body).scrollTop;
      },

      update: function update() {
        var currentScrollY = this.getScrollY(),
            scrollDirection = currentScrollY > this.lastKnownScrollY ? 'down' : 'up';

        this.onScrollCallback(currentScrollY);
        this.lastKnownScrollY = currentScrollY;
      },

      destroy: function destroy() {
        this.initialised = false;
        window.removeEventListener('scroll', this.debouncer, false);
      }

    };

    Scrllr.options = {
      onScrollCallback: function onScrollCallback() {}
    };

    module.exports = Scrllr;
  }, { "./Debouncer": 15, "lodash.assign": 10, "lodash.clone": 11, "lodash.foreach": 12, "lodash.isempty": 13, "lodash.isequal": 14 }], 17: [function (require, module, exports) {
    var Scrllr = require("./Scrllr.js"),
        Scale = require("d3-scale"),
        Interpolator = require("d3-interpolate"),
        Ease = require("d3-ease");

    function ScrollOver(options) {
      options = extend(options, ScrollOver.options);
      this.PROPERTIES = ['translateX', 'translateY', 'opacity', 'scale'];
      this.keyframes = options.keyframes;
    }

    ScrollOver.prototype = {
      constructor: ScrollOver,

      init: function init() {
        var _this = this;

        new Scrllr({ onScrollCallback: update.bind(this) }).init();

        this.toAnimate = this.keyframes.filter(function (item) {
          return item.animate;
        });
        this.toReveal = this.keyframes.filter(function (item) {
          return item.reveal;
        });
        this.toShow = this.keyframes.filter(function (item) {
          return item.show;
        });

        this.toAnimate.forEach(function (keyframe) {
          if (keyframe) keyframe.animate.forEach(function (property) {
            property.scale = _this.createScale(property.property, keyframe.domain, property.range);
          });
        });

        function update(scrollY) {
          var _this2 = this;

          this.toAnimate.forEach(function (keyframe) {
            if (keyframe) _this2.updateCSSValues(keyframe.element, _this2.calculatePropertyValues(keyframe.animate, scrollY));
          });

          this.toReveal.forEach(function (keyframe) {
            if (keyframe) {
              if (scrollY >= keyframe.reveal.when) _this2.updateCSSClass(keyframe.element, keyframe.reveal.className);
            }
          });

          this.toShow.forEach(function (keyframe) {
            if (keyframe) {
              if (scrollY >= keyframe.show.when[0]) _this2.updateCSSClass(keyframe.element, keyframe.show.className);
              if (scrollY <= keyframe.show.when[0] || scrollY >= keyframe.show.when[1]) _this2.removeCSSClass(keyframe.element, keyframe.show.className);
            }
          });
        }

        return this;
      },

      calculatePropertyValues: function calculatePropertyValues(animations, scrollY) {
        var _this3 = this;

        var CSSValues = new Object();

        this.PROPERTIES.forEach(function (propertyName) {
          CSSValues[propertyName] = _this3.getDefaultPropertyValue(propertyName);
          animations.forEach(function (animation) {
            if (animation.property == propertyName) CSSValues[propertyName] = _this3.scaleValue(animation.scale, scrollY);
          });
        });

        return CSSValues;
      },

      scaleValue: function scaleValue(scale, scrollY) {
        return scale(scrollY);
      },

      updateCSSValues: function updateCSSValues(element, CSS) {
        element.style.transform = 'translate3d(' + CSS.translateX + 'px, ' + CSS.translateY + 'px, 0) scale(' + CSS.scale + ')';
        element.style.opacity = CSS.opacity;

        return element;
      },

      updateCSSClass: function updateCSSClass(element, className) {
        element.classList ? element.classList.add(className) : element.className += ' ' + className;

        return element;
      },

      removeCSSClass: function removeCSSClass(element, className) {
        if (element.classList) {
          element.classList.remove(className);
        } else {
          element.className = element.className.replace(new RegExp('(^|\\b)' + className.split(' ').join('|') + '(\\b|$)', 'gi'), ' ');
        }

        return element;
      },

      getDefaultPropertyValue: function getDefaultPropertyValue(propertyName) {
        switch (propertyName) {
          case 'translateX':
            return 0;
          case 'translateY':
            return 0;
          case 'scale':
            return 1;
          case 'rotate':
            return 0;
          case 'opacity':
            return 1;
          default:
            return null;
        }
      },

      createScale: function createScale(propertyName, domain, range) {
        switch (propertyName) {
          case 'translateX':
          case 'translateY':
          case 'scale':
          case 'opacity':
            return Scale.scaleLinear().domain(domain).range(range).interpolate(this.easeInterpolate(Ease.easeCubicOut)).clamp(true);
          default:
            return null;
        }
      },

      easeInterpolate: function easeInterpolate(ease) {
        return function (a, b) {
          var i = Interpolator.interpolate(a, b);
          return function (t) {
            return Math.round(i(ease(t)) * 100) / 100;
          };
        };
      }

    };

    ScrollOver.options = {
      keyframes: {}

      /**
       * Helper function for extending objects
       */
    };function extend(object /*, objectN ... */) {
      if (arguments.length <= 0) {
        throw new Error('Missing arguments in extend function');
      }

      var result = object || {},
          key,
          i;

      for (i = 1; i < arguments.length; i++) {
        var replacement = arguments[i] || {};

        for (key in replacement) {
          // Recurse into object except if the object is a DOM element
          if (_typeof(result[key]) === 'object' && !isDOMElement(result[key])) {
            result[key] = extend(result[key], replacement[key]);
          } else {
            result[key] = result[key] || replacement[key];
          }
        }
      }

      return result;
    }

    /**
     * Check if object is part of the DOM
     * @constructor
     * @param {Object} obj element to check
     */
    function isDOMElement(obj) {
      return obj && typeof window !== 'undefined' && (obj === window || obj.nodeType);
    }

    module.exports = ScrollOver;
  }, { "./Scrllr.js": 16, "d3-ease": 4, "d3-interpolate": 6, "d3-scale": 7 }], 18: [function (require, module, exports) {
    // AnimateScroll.js
    // Sunmock Yang Nov. 2015

    function animateScroll(target, duration, easing, padding, align, onFinish) {
      padding = padding ? padding : 0;
      var docElem = document.documentElement; // to facilitate minification better
      var windowHeight = docElem.clientHeight;
      var maxScroll = 'scrollMaxY' in window ? window.scrollMaxY : docElem.scrollHeight - windowHeight;
      var currentY = window.pageYOffset;

      var targetY = currentY;
      var elementBounds = isNaN(target) ? target.getBoundingClientRect() : 0;

      if (align === "center") {
        targetY += isNaN(target) ? elementBounds.top + elementBounds.height / 2 : target;
        targetY -= windowHeight / 2;
        targetY -= padding;
      } else if (align === "bottom") {
        targetY += elementBounds.bottom || target;
        targetY -= windowHeight;
        targetY += padding;
      } else {
        // top, undefined
        targetY += elementBounds.top || target;
        targetY -= padding;
      }
      targetY = Math.max(Math.min(maxScroll, targetY), 0);

      var deltaY = targetY - currentY;

      var obj = {
        targetY: targetY,
        deltaY: deltaY,
        duration: duration ? duration : 0,
        easing: easing in animateScroll.Easing ? animateScroll.Easing[easing] : animateScroll.Easing.linear,
        onFinish: onFinish,
        startTime: Date.now(),
        lastY: currentY,
        step: animateScroll.step
      };

      window.requestAnimationFrame(obj.step.bind(obj));
    }

    // Taken from gre/easing.js
    // https://gist.github.com/gre/1650294
    animateScroll.Easing = {
      linear: function linear(t) {
        return t;
      },
      easeInQuad: function easeInQuad(t) {
        return t * t;
      },
      easeOutQuad: function easeOutQuad(t) {
        return t * (2 - t);
      },
      easeInOutQuad: function easeInOutQuad(t) {
        return t < .5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
      },
      easeInCubic: function easeInCubic(t) {
        return t * t * t;
      },
      easeOutCubic: function easeOutCubic(t) {
        return --t * t * t + 1;
      },
      easeInOutCubic: function easeInOutCubic(t) {
        return t < .5 ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;
      },
      easeInQuart: function easeInQuart(t) {
        return t * t * t * t;
      },
      easeOutQuart: function easeOutQuart(t) {
        return 1 - --t * t * t * t;
      },
      easeInOutQuart: function easeInOutQuart(t) {
        return t < .5 ? 8 * t * t * t * t : 1 - 8 * --t * t * t * t;
      },
      easeInQuint: function easeInQuint(t) {
        return t * t * t * t * t;
      },
      easeOutQuint: function easeOutQuint(t) {
        return 1 + --t * t * t * t * t;
      },
      easeInOutQuint: function easeInOutQuint(t) {
        return t < .5 ? 16 * t * t * t * t * t : 1 + 16 * --t * t * t * t * t;
      }
    };

    animateScroll.step = function () {
      if (this.lastY !== window.pageYOffset && this.onFinish) {
        this.onFinish();
        return;
      }

      // Calculate how much time has passed
      var t = Math.min((Date.now() - this.startTime) / this.duration, 1);

      // Scroll window amount determined by easing
      var y = this.targetY - (1 - this.easing(t)) * this.deltaY;
      window.scrollTo(window.scrollX, y);

      // Continue animation as long as duration hasn't surpassed
      if (t !== 1) {
        this.lastY = window.pageYOffset;
        window.requestAnimationFrame(this.step.bind(this));
      } else {
        if (this.onFinish) this.onFinish();
      }
    };

    module.exports = animateScroll;
  }, {}], 19: [function (require, module, exports) {
    var ScrollOver = require("./lib/ScrollOver.js");
    var animateScroll = require("./lib/animatescroll.js");

    var body = document.querySelectorAll("body")[0];
    // let widget = document.querySelectorAll(".widget-wrap")[0]
    // let tagline = document.querySelectorAll(".tagline")[0]
    var slideOne = document.querySelectorAll(".slide--one")[0];
    var slideTwo = document.querySelectorAll(".slide--two")[0];
    var sectionOne = document.querySelectorAll(".section--one")[0];
    var sectionTwo = document.querySelectorAll(".section--two")[0];
    var sectionThree = document.querySelectorAll(".section--three")[0];
    var sectionFour = document.querySelectorAll(".section--four")[0];
    // let slideTwo = document.querySelectorAll(".slide--two")[0]
    // let slideFive = document.querySelectorAll(".slide--five")[0]
    // let phone = document.querySelectorAll(".app-inner")[0]
    var scrollTopButton = document.querySelectorAll(".button--scrolltop")[0];
    var scrollToFeatures = document.querySelectorAll(".nav__item-features")[0];
    //
    setTimeout(function () {
      return body.classList.add("shown");
    }, 400);
    //

    scrollTopButton.addEventListener('click', function (event) {
      animateScroll(slideOne, 600, "easeInOutCubic", 0);
      event.preventDefault();
    });

    scrollToFeatures.addEventListener('click', function (event) {
      animateScroll(slideTwo, 600, "easeInOutCubic", 0);
      event.preventDefault();
    });

    //
    // if(document.querySelectorAll(".button--unavailable")[0]) {
    //   document.querySelectorAll(".button--unavailable")[0].addEventListener('click', function(event){
    //     event.preventDefault()
    //   })
    // }
    //
    // if(document.querySelectorAll(".button--prtcpt")[0]) {
    //   document.querySelectorAll(".button--prtcpt")[0].addEventListener('click', function(event){
    //       animateScroll(slideTwo, 600, "easeInOutCubic", 0)
    //       event.preventDefault()
    //   })
    // }
    //
    // if(document.querySelectorAll(".nav__item--participate")[0]) {
    //   document.querySelectorAll(".nav__item--participate")[0].addEventListener('click', function(event){
    //       animateScroll(slideTwo, 600, "easeInOutCubic", 0)
    //       event.preventDefault()
    //   })
    // }
    //
    //
    new ScrollOver({
      keyframes: [{
        element: sectionOne,
        reveal: {
          when: 120,
          className: "section--shown"
        }
      }, {
        element: sectionTwo,
        reveal: {
          when: 500,
          className: "section--shown"
        }
      }, {
        element: sectionThree,
        reveal: {
          when: 900,
          className: "section--shown"
        }
      }, {
        element: sectionFour,
        reveal: {
          when: 1280,
          className: "section--shown"
        }
      }]
    }).init();

    /*---Utils---*/
    function addClassToElement(element, className) {
      element.classList ? element.classList.add(className) : element.className += ' ' + className;
      return element;
    }

    function removeClassFromElement(element, className) {
      if (element.classList) {
        element.classList.remove(className);
      } else {
        element.className = element.className.replace(new RegExp('(^|\\b)' + className.split(' ').join('|') + '(\\b|$)', 'gi'), ' ');
      }
      return element;
    }
  }, { "./lib/ScrollOver.js": 17, "./lib/animatescroll.js": 18 }] }, {}, [19]);