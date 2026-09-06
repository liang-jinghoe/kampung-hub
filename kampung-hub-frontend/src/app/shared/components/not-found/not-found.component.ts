import { Component } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';

// FE-Req-11: 404 Wildcard Route Component
@Component({
  selector: 'app-not-found',
  standalone: true,
  imports: [CommonModule, RouterModule],
  template: `
    <div class="not-found-container container">
      <div class="card not-found-card">
        <div class="error-code">404</div>
        <h1 class="error-title">Page Not Found</h1>
        <p class="error-desc">
          The page or security resource you requested does not exist or has been relocated.
        </p>
        <div class="action-row">
          <a routerLink="/dashboard" class="btn btn-primary">
            <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 9l9-7 9 7v11a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2z"/>
            </svg>
            Back to Dashboard
          </a>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .not-found-container {
      min-height: calc(100vh - 120px);
      display: flex;
      align-items: center;
      justify-content: center;
      padding: var(--spacing-xl);
    }
    .not-found-card {
      text-align: center;
      padding: 48px;
      max-width: 500px;
      width: 100%;
    }
    .error-code {
      font-size: 5rem;
      font-weight: 900;
      font-family: var(--font-heading);
      background: linear-gradient(135deg, var(--primary) 0%, #3b82f6 100%);
      -webkit-background-clip: text;
      -webkit-text-fill-color: transparent;
      line-height: 1;
      margin-bottom: 8px;
    }
    .error-title {
      font-size: 1.5rem;
      font-weight: 700;
      color: var(--text-main);
      margin-bottom: 8px;
    }
    .error-desc {
      color: var(--text-muted);
      font-size: 0.9375rem;
      margin-bottom: 24px;
    }
    .action-row {
      display: flex;
      justify-content: center;
    }
  `]
})
export class NotFoundComponent {}
