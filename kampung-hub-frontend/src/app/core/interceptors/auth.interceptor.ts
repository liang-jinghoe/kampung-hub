import { HttpInterceptorFn } from '@angular/common/http';

// FE-Req-10: Functional HTTP Interceptor injecting Bearer JWT token into outgoing requests
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const token = localStorage.getItem('kampung_token');
  if (token) {
    const cloned = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
    return next(cloned);
  }
  return next(req);
};
