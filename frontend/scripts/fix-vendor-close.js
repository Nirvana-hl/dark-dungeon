const fs = require('fs');
const path = require('path');

// Recursively walk directory and return file paths
function walk(dir) {
  const files = [];
  for (const name of fs.readdirSync(dir)) {
    const full = path.join(dir, name);
    const stat = fs.statSync(full);
    if (stat.isDirectory()) {
      files.push(...walk(full));
    } else {
      files.push(full);
    }
  }
  return files;
}

function fixFile(file) {
  let content = fs.readFileSync(file, 'utf8');
  const original = content;

  // Replace occurrences that call socket.close with code: 1006 or similar object
  // e.g. socket.close({ code: 1006, reason: "connect timeout" });
  content = content.replace(/socket\.close\s*\(\s*\{\s*code\s*:\s*1006[\s\S]*?\}\s*\)\s*;/g, function (m) {
    return 'try { socket.close(); } catch (e) { try { socket.close && socket.close() } catch (e2) {} }';
  });

  // Also replace patterns like socket.close({code:1006}); (without spaces)
  content = content.replace(/socket\.close\(\s*\{\s*code\s*:\s*1006\s*\}\s*\)\s*;/g, function (m) {
    return 'try { socket.close(); } catch (e) { try { socket.close && socket.close() } catch (e2) {} }';
  });

  if (content !== original) {
    fs.writeFileSync(file, content, 'utf8');
    console.log('[fix-vendor-close] Patched', file);
    return true;
  }
  return false;
}

function main() {
  const targetDir = path.join(__dirname, '..', 'unpackage');
  if (!fs.existsSync(targetDir)) {
    console.warn('[fix-vendor-close] no unpackage directory found, skipping');
    return;
  }
  const files = walk(targetDir);
  let patched = 0;
  for (const f of files) {
    try {
      if (fixFile(f)) patched++;
    } catch (e) {
      // ignore read errors
    }
  }
  console.log('[fix-vendor-close] Done. Files patched:', patched);
}

main();


























