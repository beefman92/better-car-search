import React from "react";
import { Snackbar } from "@material-ui/core";
import Alert, { Color } from "@material-ui/lab/Alert";

export type ToastState = {
  open: boolean;
  severity: Color;
  message: string;
};

type Props = {
  toastState: ToastState;
  onClose: () => void;
};

const Toast = ({ toastState, onClose }: Props) => {
  const handleClose = (event: any, reason: string) => {
    if (reason !== "clickaway") {
      onClose();
    }
  };

  return (
    <Snackbar
      open={toastState.open}
      autoHideDuration={6000}
      onClose={handleClose}
    >
      <Alert
        elevation={6}
        variant="filled"
        severity={toastState.severity}
        onClose={onClose}
      >
        {toastState.message}
      </Alert>
    </Snackbar>
  );
};

export default Toast;
