import type { ReactNode } from "react";
import styles from "./EmptyState.module.css";

type EmptyStateProps = {
  title: string;
  message: string;
  action?: ReactNode;
};

export function EmptyState({ title, message, action }: EmptyStateProps) {
  return (
    <div className={styles.emptyState}>
      <h2>{title}</h2>
      <p>{message}</p>
      {action}
    </div>
  );
}
