// sweetAlert.ts
import Swal, { SweetAlertIcon, SweetAlertOptions } from 'sweetalert2';

export function showAlert(
  options: {
    title: string;
    text?: string;
    icon?: SweetAlertIcon;
    confirmButtonText?: string;
    cancelButtonText?: string;
    showCancelButton?: boolean;
    allowOutsideClick?: boolean;
    allowEscapeKey?: boolean;
    timer?: number;
    [key: string]: any; // allows passing any other SweetAlert2 options
  },
  callback?: (result: Swal.DismissReason | Swal.ClickConfirmEvent | Swal.ClickCancelEvent) => void
): void {
  const swalOptions: SweetAlertOptions = {
    title: options.title,
    text: options.text || '',
    icon: options.icon || 'info',
    confirmButtonText: options.confirmButtonText || 'OK',
    cancelButtonText: options.cancelButtonText || 'Cancel',
    showCancelButton: options.showCancelButton || false,
    allowOutsideClick: options.allowOutsideClick ?? true,
    allowEscapeKey: options.allowEscapeKey ?? true,
    timer: options.timer,
    ...options, // merge additional custom options
  };

  Swal.fire(swalOptions).then((result) => {
    if (callback) {
      callback(result);
    }
  });
}
