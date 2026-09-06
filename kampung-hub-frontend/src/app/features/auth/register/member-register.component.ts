import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ReactiveFormsModule, FormBuilder, FormGroup, Validators, AbstractControl, ValidationErrors } from '@angular/forms';
import { Router, ActivatedRoute, RouterModule } from '@angular/router';
import { NeighborhoodService } from '../../../core/services/neighborhood.service';

// Custom validator to ensure passwords match
function matchPasswords(group: AbstractControl): ValidationErrors | null {
  const password = group.get('password')?.value;
  const confirmPassword = group.get('confirmPassword')?.value;
  return password === confirmPassword ? null : { passwordsMismatch: true };
}

// FE-Req-8, 9, 10: Public Onboarding Component activated via ?token=...
@Component({
  selector: 'app-member-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterModule],
  templateUrl: './member-register.component.html',
  styleUrls: ['./member-register.component.css']
})
export class MemberRegisterComponent implements OnInit {
  registerForm: FormGroup;
  token = '';
  isLoading = false;
  isSuccess = false;
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private neighborhoodService: NeighborhoodService,
    private route: ActivatedRoute,
    private router: Router
  ) {
    this.registerForm = this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(3)]],
      phoneNumber: ['', [Validators.required]],
      password: ['', [Validators.required, Validators.minLength(6)]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: matchPasswords });
  }

  ngOnInit(): void {
    this.token = this.route.snapshot.queryParams['token'] || '';
    if (!this.token) {
      this.errorMessage = 'Missing onboarding invitation token. Please check your invitation link.';
    }
  }

  get f() {
    return this.registerForm.controls;
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    if (!this.token) {
      this.errorMessage = 'Cannot submit onboarding without a valid token.';
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const payload = {
      fullName: this.registerForm.value.fullName,
      phoneNumber: this.registerForm.value.phoneNumber,
      password: this.registerForm.value.password
    };

    this.neighborhoodService.onboardMember(this.token, payload).subscribe({
      next: () => {
        this.isLoading = false;
        this.isSuccess = true;
      },
      error: (err) => {
        this.isLoading = false;
        this.errorMessage = err.error?.message || 'Failed to complete registration. Invitation token may have expired.';
      }
    });
  }
}
