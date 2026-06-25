// ============================================================
//  nav.js — builds the navbar based on login state.
//  Include AFTER auth.js. Put <div id="navbar"></div> in each page.
// ============================================================

function buildNavbar() {
    const loggedIn = isLoggedIn();   // from auth.js — checks for stored token
  
    // links differ depending on whether the user is logged in
    let links;
    if (loggedIn) {
      // LOGGED IN: dashboard + logout
      links = `
        <li><a href="index.html">Home</a></li>
        <li><a href="dashboard.html">Dashboard</a></li>
        <li><a href="upgrade.html">Upgrade</a></li>
        <li><a onclick="logout()" class="btn-primary nav-btn">Logout</a></li>
      `;
    } else {
      // LOGGED OUT: login + signup
      links = `
        <li><a href="index.html">Home</a></li>
        <li><a href="login.html">Log in</a></li>
        <li><a href="signup.html" class="btn-primary nav-btn">Sign up</a></li>
      `;
    }
  
    // inject the full navbar into the page
    document.getElementById('navbar').innerHTML = `
      <nav>
        <a href="index.html" class="nav-logo">Link<span>Shrink</span></a>
        <ul>${links}</ul>
      </nav>
    `;
  }
  
  // run as soon as the page loads
  document.addEventListener('DOMContentLoaded', buildNavbar);