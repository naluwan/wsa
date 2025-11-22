/**
 * 課程詳情頁面（Course Detail Page）
 * 根據課程代碼顯示課程資訊與單元列表，包含：
 * - 課程標題與描述
 * - 課程難度 badge
 * - 單元列表（顯示標題、類型、完成狀態）
 * - 進入單元按鈕
 *
 * 資料來源：後端 API /api/courses/{courseCode}（真實資料）
 */
import Link from "next/link"
import { notFound } from "next/navigation"
import { ArrowLeft, ArrowRight, BookOpen, CheckCircle2, Circle, PlayCircle } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"

// 強制此頁面為動態路由，不要快取
export const dynamic = 'force-dynamic';

/**
 * 單元資料型別（對應後端 UnitSummaryDto）
 */
interface UnitSummary {
  id: string;
  unitId: string;
  title: string;
  type: string;
  orderIndex: number;
  isCompleted: boolean;
}

/**
 * 課程資料型別（對應後端 CourseDto，包含單元列表）
 */
interface CourseDetail {
  id: string;
  code: string;
  title: string;
  description: string;
  levelTag: string;
  totalUnits: number;
  coverIcon: string;
  units: UnitSummary[];
}

/**
 * 將 levelTag 轉換為顯示文字
 */
function getLevelText(levelTag: string): string {
  const levelMap: Record<string, string> = {
    beginner: "初級",
    intermediate: "中級",
    advanced: "進階",
  };
  return levelMap[levelTag] || levelTag;
}

/**
 * 取得等級對應的 badge variant
 */
function getLevelVariant(levelTag: string): "default" | "secondary" | "destructive" | "outline" {
  switch (levelTag) {
    case "beginner":
      return "secondary"
    case "intermediate":
      return "default"
    case "advanced":
      return "destructive"
    default:
      return "outline"
  }
}

/**
 * 根據單元類型取得圖示
 */
function getUnitIcon(type: string) {
  switch (type) {
    case "video":
      return PlayCircle;
    default:
      return BookOpen;
  }
}

/**
 * 從前端 API Route 獲取課程詳情
 */
async function getCourseDetail(courseCode: string): Promise<CourseDetail | null> {
  try {
    // 呼叫前端 API Route，由它處理 cookie 與後端通訊
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:3000";
    const res = await fetch(`${apiUrl}/api/courses/${courseCode}`, {
      cache: "no-store", // 不使用快取，確保資料是最新的
    });

    if (res.status === 404) {
      return null;
    }

    if (!res.ok) {
      console.error("[courses/[courseCode]/page] 取得課程詳情失敗:", res.status);
      return null;
    }

    const course: CourseDetail = await res.json();
    return course;
  } catch (error) {
    console.error("[courses/[courseCode]/page] 取得課程詳情發生錯誤:", error);
    return null;
  }
}

interface PageProps {
  params: Promise<{
    courseCode: string;
  }>;
}

export default async function CourseDetailPage({ params }: PageProps) {
  const { courseCode } = await params;
  const course = await getCourseDetail(courseCode);

  // 若課程不存在，顯示 404
  if (!course) {
    notFound();
  }

  const levelText = getLevelText(course.levelTag);
  const completedUnits = course.units.filter(u => u.isCompleted).length;
  const progress = course.totalUnits > 0 ? Math.round((completedUnits / course.totalUnits) * 100) : 0;

  return (
    <div className="flex flex-col">
      {/* 返回按鈕與課程標題 */}
      <section className="w-full py-8 md:py-12 bg-gradient-to-b from-muted/50 to-background">
        <div className="container px-4 md:px-6 max-w-5xl">
          <Button variant="ghost" asChild className="mb-6">
            <Link href="/courses">
              <ArrowLeft className="mr-2 h-4 w-4" />
              返回課程列表
            </Link>
          </Button>

          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <Badge variant={getLevelVariant(course.levelTag)}>{levelText}</Badge>
              <span className="text-sm text-muted-foreground">
                {completedUnits} / {course.totalUnits} 單元已完成（{progress}%）
              </span>
            </div>
            <h1 className="text-3xl font-bold tracking-tighter sm:text-4xl md:text-5xl">
              {course.title}
            </h1>
            <p className="text-lg text-muted-foreground max-w-[700px]">
              {course.description}
            </p>
          </div>
        </div>
      </section>

      {/* 單元列表 */}
      <section className="w-full py-8 md:py-12">
        <div className="container px-4 md:px-6 max-w-5xl">
          <h2 className="text-2xl font-bold mb-6">課程單元</h2>

          {course.units.length === 0 ? (
            <div className="text-center py-12 border rounded-lg bg-muted/30">
              <BookOpen className="mx-auto h-12 w-12 text-muted-foreground mb-4" />
              <p className="text-muted-foreground">此課程尚未建立單元</p>
            </div>
          ) : (
            <div className="space-y-4">
              {course.units.map((unit) => {
                const UnitIcon = getUnitIcon(unit.type);

                return (
                  <Card
                    key={unit.id}
                    className="hover:shadow-md transition-shadow"
                    data-testid="unit-card"
                  >
                    <CardHeader className="pb-3">
                      <div className="flex items-start justify-between gap-4">
                        <div className="flex items-start gap-3 flex-1">
                          {/* 完成狀態圖示 */}
                          {unit.isCompleted ? (
                            <CheckCircle2 className="h-5 w-5 text-green-500 mt-1 flex-shrink-0" />
                          ) : (
                            <Circle className="h-5 w-5 text-muted-foreground mt-1 flex-shrink-0" />
                          )}

                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-1">
                              <UnitIcon className="h-4 w-4 text-muted-foreground" />
                              <span className="text-xs text-muted-foreground uppercase">
                                {unit.type === "video" ? "影片" : unit.type}
                              </span>
                            </div>
                            <CardTitle
                              className="text-lg"
                              data-testid="unit-title"
                            >
                              {unit.title}
                            </CardTitle>
                          </div>
                        </div>

                        <Button
                          asChild
                          variant={unit.isCompleted ? "outline" : "default"}
                          data-testid="enter-unit-button"
                        >
                          <Link href={`/units/${unit.unitId}`}>
                            {unit.isCompleted ? "重新觀看" : "開始學習"}
                            <ArrowRight className="ml-2 h-4 w-4" />
                          </Link>
                        </Button>
                      </div>
                    </CardHeader>
                  </Card>
                )
              })}
            </div>
          )}
        </div>
      </section>
    </div>
  )
}
