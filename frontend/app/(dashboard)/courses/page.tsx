/**
 * 課程頁面（Courses Page）
 * 顯示所有可用課程，包含：
 * - 課程封面圖片
 * - 課程名稱與描述
 * - 課程等級 badge
 * - 進入課程按鈕
 *
 * 資料來源：後端 API /api/courses（真實資料）
 */
import Link from "next/link"
import { ArrowRight, BookOpen, Code, Database, Palette, Rocket, Shapes } from "lucide-react"

import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"

// 強制此頁面為動態路由，不要快取
export const dynamic = 'force-dynamic';

/**
 * 課程資料型別（對應後端 CourseDto）
 */
interface Course {
  id: string;
  code: string;
  title: string;
  description: string;
  levelTag: string;
  totalUnits: number;
  coverIcon: string;
}

/**
 * 根據 coverIcon 取得對應的 Lucide 圖示
 */
function getCourseIcon(coverIcon: string) {
  const iconMap: Record<string, any> = {
    backend_java: Code,
    frontend_react: Palette,
    software_design_pattern: Shapes,
    database: Database,
    devops: Rocket,
    default: BookOpen,
  };
  return iconMap[coverIcon] || iconMap.default;
}

/**
 * 根據 coverIcon 取得對應的漸層顏色
 */
function getCourseColor(coverIcon: string): string {
  const colorMap: Record<string, string> = {
    backend_java: "from-blue-500 to-cyan-500",
    frontend_react: "from-purple-500 to-pink-500",
    software_design_pattern: "from-indigo-500 to-violet-500",
    database: "from-green-500 to-emerald-500",
    devops: "from-red-500 to-rose-500",
    default: "from-yellow-500 to-orange-500",
  };
  return colorMap[coverIcon] || colorMap.default;
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
 * 從前端 API Route 獲取課程列表
 */
async function getCourses(): Promise<Course[]> {
  try {
    // 呼叫前端 API Route，由它處理 cookie 與後端通訊
    const apiUrl = process.env.NEXT_PUBLIC_API_URL || "http://localhost:3000";
    const res = await fetch(`${apiUrl}/api/courses`, {
      cache: "no-store", // 不使用快取，確保資料是最新的
    });

    if (!res.ok) {
      console.error("[courses/page] 取得課程列表失敗:", res.status);
      return [];
    }

    const courses: Course[] = await res.json();
    return courses;
  } catch (error) {
    console.error("[courses/page] 取得課程列表發生錯誤:", error);
    return [];
  }
}

export default async function CoursesPage() {
  // 從後端 API 獲取課程列表
  const courses = await getCourses();

  return (
    <div className="flex flex-col">
      {/* 頁面標題 */}
      <section className="w-full py-12 md:py-16 bg-gradient-to-b from-muted/50 to-background">
        <div className="container px-4 md:px-6">
          <div className="flex flex-col items-center space-y-4 text-center">
            <h1 className="text-3xl font-bold tracking-tighter sm:text-4xl md:text-5xl">
              所有課程
            </h1>
            <p className="mx-auto max-w-[700px] text-muted-foreground md:text-xl">
              選擇您感興趣的課程，開始學習之旅。所有課程都提供完整的學習路徑與實戰專案。
            </p>
          </div>
        </div>
      </section>

      {/* 課程列表 */}
      <section className="w-full py-12 md:py-16">
        <div className="container px-4 md:px-6">
          {courses.length === 0 ? (
            // 無課程時的提示
            <div className="text-center py-12">
              <p className="text-muted-foreground text-lg">目前尚無可用課程</p>
            </div>
          ) : (
            <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3 max-w-7xl mx-auto">
              {courses.map((course) => {
                const Icon = getCourseIcon(course.coverIcon);
                const color = getCourseColor(course.coverIcon);
                const levelText = getLevelText(course.levelTag);

                return (
                  <Card
                    key={course.id}
                    className="flex flex-col overflow-hidden"
                    data-testid="course-card"
                  >
                    {/* 課程封面（使用漸層背景） */}
                    <div className={`h-32 bg-gradient-to-br ${color} relative`}>
                      <div className="absolute inset-0 flex items-center justify-center">
                        <Icon className="h-16 w-16 text-white/90" />
                      </div>
                    </div>

                    <CardHeader className="pb-3">
                      <div className="flex items-center justify-between mb-2">
                        <Badge variant={getLevelVariant(course.levelTag)}>{levelText}</Badge>
                        <span className="text-xs text-muted-foreground">
                          {course.totalUnits} 單元
                        </span>
                      </div>
                      <CardTitle
                        className="text-xl line-clamp-2"
                        data-testid="course-title"
                      >
                        {course.title}
                      </CardTitle>
                      <CardDescription className="line-clamp-3">
                        {course.description}
                      </CardDescription>
                    </CardHeader>

                    <CardFooter className="mt-auto pt-3">
                      <Button
                        asChild
                        className="w-full"
                        data-testid="enter-course-button"
                      >
                        <Link href={`/courses/${course.code}`}>
                          開始學習
                          <ArrowRight className="ml-2 h-4 w-4" />
                        </Link>
                      </Button>
                    </CardFooter>
                  </Card>
                )
              })}
            </div>
          )}

          {/* 額外資訊 */}
          <div className="mt-12 text-center">
            <p className="text-muted-foreground">
              找不到適合的課程？{" "}
              <Link href="/contact" className="text-primary hover:underline">
                聯絡我們
              </Link>{" "}
              提出您的需求
            </p>
          </div>
        </div>
      </section>
    </div>
  )
}
