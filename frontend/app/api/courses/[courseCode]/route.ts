import { NextRequest, NextResponse } from "next/server";
import { cookies } from "next/headers";

/**
 * 取得單一課程詳情 API（Next.js Route Handler）
 *
 * 功能說明：
 * - 從 httpOnly cookie 中讀取 JWT token
 * - 向後端 /api/courses/{courseCode} 發送請求取得課程詳情
 * - 包含課程基本資訊與單元列表
 * - 回傳課程詳情給前端元件使用
 *
 * 資料來源：後端 /api/courses/{courseCode}（真實資料）
 *
 * 使用場景：
 * - /courses/[courseCode] 頁面顯示課程詳情與單元列表
 *
 * 錯誤處理：
 * - 無 token → 回傳 401 未授權
 * - token 無效或過期 → 回傳 401 未授權
 * - 課程不存在 → 回傳 404
 * - 其他錯誤 → 回傳 500 伺服器錯誤
 */

// 強制此 API route 為動態路由，不要快取
export const dynamic = 'force-dynamic';

export async function GET(
  request: NextRequest,
  { params }: { params: Promise<{ courseCode: string }> }
) {
  try {
    const { courseCode } = await params;

    // 步驟 1: 從 cookie 中取得 JWT token
    const cookieStore = await cookies();
    const token = cookieStore.get("token")?.value;

    console.log("[/api/courses/[courseCode]] Token 存在:", !!token);
    console.log("[/api/courses/[courseCode]] 課程代碼:", courseCode);

    // 步驟 2: 若沒有 token，回傳 401
    if (!token) {
      console.log("[/api/courses/[courseCode]] 沒有 token，回傳 401");
      return NextResponse.json({ error: "未授權" }, { status: 401 });
    }

    // 步驟 3: 使用 token 向後端 API 請求課程詳情
    const apiUrl = process.env.API_URL || process.env.NEXT_PUBLIC_API_URL || "http://localhost:8080";
    console.log("[/api/courses/[courseCode]] 向後端發送請求到:", `${apiUrl}/api/courses/${courseCode}`);

    const response = await fetch(
      `${apiUrl}/api/courses/${courseCode}`,
      {
        headers: {
          Authorization: `Bearer ${token}`,
        },
      }
    );

    console.log("[/api/courses/[courseCode]] 後端回應狀態:", response.status);

    // 步驟 4: 若後端回傳錯誤，回傳對應的錯誤狀態
    if (!response.ok) {
      const errorText = await response.text();
      console.error("[/api/courses/[courseCode]] 後端回應錯誤:", response.status, errorText);
      return NextResponse.json(
        { error: response.status === 404 ? "找不到課程" : "取得課程詳情失敗" },
        { status: response.status }
      );
    }

    // 步驟 5: 取得課程詳情並回傳給前端
    const course = await response.json();
    console.log("[/api/courses/[courseCode]] 成功取得課程詳情:", course.title);
    return NextResponse.json(course, { status: 200 });
  } catch (error) {
    console.error("[/api/courses/[courseCode]] 發生錯誤:", error);
    return NextResponse.json(
      { error: "伺服器錯誤" },
      { status: 500 }
    );
  }
}
