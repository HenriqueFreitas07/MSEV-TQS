import Swal, { type SweetAlertIcon, type SweetAlertResult } from 'sweetalert2';

// ======== ALERT FUNCTIONS ========

/**
 * Show a simple alert
 */
export function showAlert(
  title: string,
  text: string,
  icon: SweetAlertIcon = 'info'
): void {
  Swal.fire({
    title,
    text,
    icon,
    confirmButtonText: 'OK',
  });
}

/**
 * Show a confirmation alert with "Yes/Cancel"
 * Returns a Promise that resolves to the SweetAlertResult
 */
export function showConfirmAlert(
  title: string,
  text: string,
  confirmButtonText: string = 'Yes',
  cancelButtonText: string = 'Cancel'
): Promise<SweetAlertResult> {
  return Swal.fire({
    title,
    text,
    icon: 'question',
    showCancelButton: true,
    confirmButtonText,
    cancelButtonText,
  });
}

/**
 * Show an alert with custom HTML content
 */
export function showHtmlAlert(
  title: string,
  htmlContent: string
): void {
  Swal.fire({
    title,
    html: htmlContent,
    icon: 'info',
    confirmButtonText: 'Got it!',
  });
}

// ======== TOAST FUNCTIONS ========

/**
 * Show a basic toast message
 */
export function showToast(
  message: string,
  icon: SweetAlertIcon = 'success',
  position: 'top' | 'top-start' | 'top-end' | 'center' | 'center-start' | 'center-end' | 'bottom' | 'bottom-start' | 'bottom-end' = 'top-end'
): void {
  Swal.fire({
    toast: true,
    position,
    icon,
    title: message,
    showConfirmButton: false,
    timer: 3000,
    timerProgressBar: true,
  });
}

/**
 * Show a customizable toast with full options
 */
export interface CustomToastOptions {
  message: string;
  icon?: SweetAlertIcon;
  position?: 'top' | 'top-start' | 'top-end' | 'center' | 'center-start' | 'center-end' | 'bottom' | 'bottom-start' | 'bottom-end';
  timer?: number;
}

export function showCustomToast({
  message,
  icon = 'info',
  position = 'top-end',
  timer = 3000,
}: CustomToastOptions): void {
  Swal.fire({
    toast: true,
    position,
    icon,
    title: message,
    showConfirmButton: false,
    timer,
    timerProgressBar: true,
  });
}
