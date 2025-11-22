/**
 * DashboardLayout - Dashboard 頁面佈局
 * 包含側邊欄的頁面使用此佈局
 */
import { DashboardLayout } from '@/components/dashboard-layout';

export default function Layout({
  children,
}: {
  children: React.ReactNode;
}) {
  return <DashboardLayout>{children}</DashboardLayout>;
}
