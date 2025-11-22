/**
 * 課程學習頁（Journey Player Page）
 *
 * 功能：
 * - 左側：課程單元清單 Sidebar（可收合，包含 Logo）
 * - 右側：全屏播放器（自訂控制列）
 * - 頂部：SiteHeader（包含通知、頭像等，來自 Root Layout）
 * - 支援試看單元、未購買鎖定提示、完成單元與 XP 更新
 * - 使用與首頁相同的 layout 結構
 *
 * 路由：/journeys/[courseCode]/missions/[unitId]
 */
"use client"

import { useState, useEffect, useRef, useMemo } from "react"
import { useRouter, useParams } from "next/navigation"
import { Lock, X, Play, Pause, Volume2, VolumeX, Maximize, SkipForward } from "lucide-react"
import dynamic from "next/dynamic"

import { Button } from "@/components/ui/button"
import { Card } from "@/components/ui/card"
import { Alert, AlertDescription } from "@/components/ui/alert"
import {
  AlertDialog,
  AlertDialogAction,
  AlertDialogCancel,
  AlertDialogContent,
  AlertDialogDescription,
  AlertDialogFooter,
  AlertDialogHeader,
  AlertDialogTitle,
} from "@/components/ui/alert-dialog"
import { useToast } from "@/hooks/use-toast"
import { CourseUnitSidebar } from "@/components/course-unit-sidebar"
import { cn } from "@/lib/utils"
import { useSidebar } from "@/contexts/sidebar-context"

// 動態導入 ReactPlayer（僅客戶端）
const ReactPlayer = dynamic(() => import("react-player"), {
  ssr: false,
})

/**
 * 單元詳情型別
 */
interface UnitDetail {
  id: string
  unitId: string
  courseCode: string
  courseTitle: string
  title: string
  type: string
  videoUrl: string
  xpReward: number
  isFreePreview: boolean
  canAccess: boolean
  isCompleted: boolean
}

/**
 * 完成單元回應型別
 */
interface CompleteUnitResponse {
  unitId: string
  xpEarned: number
  user: {
    level: number
    totalXp: number
    weeklyXp: number
  }
}

export default function JourneyPlayerPage() {
  const router = useRouter()
  const { toast } = useToast()
  const params = useParams()
  const { isCollapsed } = useSidebar()

  // 解析 URL 參數
  const courseCode = params.courseCode as string
  const unitId = params.unitId as string

  // 資料狀態
  const [currentUnit, setCurrentUnit] = useState<UnitDetail | null>(null)
  const [isLoggedIn, setIsLoggedIn] = useState<boolean>(false)
  const [loading, setLoading] = useState<boolean>(true)

  // UI 狀態
  const [showLoginDialog, setShowLoginDialog] = useState<boolean>(false)
  const [completing, setCompleting] = useState<boolean>(false)
  const [showCouponAlert, setShowCouponAlert] = useState<boolean>(true)

  // 播放器狀態
  const playerRef = useRef<any>(null)
  const playerContainerRef = useRef<HTMLDivElement>(null)
  const [playing, setPlaying] = useState<boolean>(false)
  const [volume, setVolume] = useState<number>(0.8)
  const [muted, setMuted] = useState<boolean>(false)

  // 檢查登入狀態
  useEffect(() => {
    async function checkAuth() {
      try {
        const res = await fetch('/api/auth/me')
        setIsLoggedIn(res.ok)
      } catch {
        setIsLoggedIn(false)
      }
    }
    checkAuth()
  }, [])

  // 載入當前單元詳情
  useEffect(() => {
    if (!unitId) return

    async function fetchUnitDetail() {
      try {
        setLoading(true)
        const res = await fetch(`/api/units/${unitId}`)
        if (res.ok) {
          const data: UnitDetail = await res.json()
          setCurrentUnit(data)
        }
      } catch (error) {
        console.error('載入單元詳情失敗:', error)
      } finally {
        setLoading(false)
      }
    }
    fetchUnitDetail()
  }, [unitId])

  /**
   * 處理完成單元
   */
  const handleCompleteUnit = async () => {
    if (!currentUnit || completing) return

    try {
      setCompleting(true)
      const res = await fetch(`/api/units/${currentUnit.unitId}/complete`, {
        method: 'POST',
      })

      if (res.ok) {
        const data: CompleteUnitResponse = await res.json()

        // 顯示成功訊息
        toast({
          title: "單元已完成！",
          description: `獲得 ${data.xpEarned} XP`,
        })

        // 更新當前單元狀態
        setCurrentUnit(prev => prev ? { ...prev, isCompleted: true } : null)
      } else if (res.status === 401) {
        toast({
          title: "請先登入",
          description: "完成單元需要登入帳號",
          variant: "destructive",
        })
      } else {
        toast({
          title: "完成失敗",
          description: "無法完成此單元，請稍後再試",
          variant: "destructive",
        })
      }
    } catch (error) {
      console.error('完成單元失敗:', error)
      toast({
        title: "發生錯誤",
        description: "請稍後再試",
        variant: "destructive",
      })
    } finally {
      setCompleting(false)
    }
  }

  /**
   * 播放器控制函數
   */
  const handlePlayPause = () => {
    setPlaying(!playing)
  }

  const handleVolumeToggle = () => {
    setMuted(!muted)
  }

  const handleVolumeChange = (newVolume: number) => {
    setVolume(newVolume)
    setMuted(newVolume === 0)
  }

  const handleFullscreen = () => {
    if (playerContainerRef.current) {
      if (document.fullscreenElement) {
        document.exitFullscreen()
      } else {
        playerContainerRef.current.requestFullscreen()
      }
    }
  }

  const handleNextUnit = () => {
    // TODO: 實現下一部影片功能（需要從課程詳情中獲取下一個單元）
    toast({
      title: "功能開發中",
      description: "下一部影片功能即將推出",
    })
  }

  /**
   * 從 YouTube URL 提取 Video ID
   */
  const getYouTubeVideoId = (url: string): string | null => {
    if (!url) return null
    try {
      const urlObj = new URL(url)

      // 處理 youtu.be 短網址
      if (urlObj.hostname === 'youtu.be') {
        return urlObj.pathname.slice(1) // 移除開頭的 /
      }

      // 處理標準 YouTube URL
      if (urlObj.hostname.includes('youtube.com')) {
        return urlObj.searchParams.get('v')
      }

      return null
    } catch {
      return null
    }
  }

  // 調試：在載入完成後輸出單元資訊
  useEffect(() => {
    if (currentUnit) {
      console.log('=== 當前單元資訊 ===')
      console.log('單元 ID:', currentUnit.unitId)
      console.log('原始影片 URL:', currentUnit.videoUrl)
      console.log('Video ID:', getYouTubeVideoId(currentUnit.videoUrl))
      console.log('canAccess:', currentUnit.canAccess)
      console.log('==================')
    }
  }, [currentUnit])

  if (loading || !currentUnit) {
    return (
      <div className="flex h-screen items-center justify-center">
        <p className="text-muted-foreground">載入中...</p>
      </div>
    )
  }

  return (
    <>
      {/* 課程單元 Sidebar */}
      <CourseUnitSidebar
        courseCode={courseCode}
        currentUnitId={unitId}
      />

      {/* 主內容區（根據 sidebar 狀態調整左邊距）*/}
      <div
        className={cn(
          "flex-1 transition-all duration-300",
          isCollapsed ? "ml-0" : "ml-64"
        )}
      >
        {/* SiteHeader 已經在 Root Layout 中，高度為 16（64px）*/}
        <main className="min-h-screen pt-16">
          {/* 播放器區域（佔滿整個可視區域）*/}
          <div className="h-[calc(100vh-4rem)] bg-black relative flex flex-col">
            {currentUnit.canAccess ? (
              <>
                {/* 折價券提示 Alert（浮在播放器上方） */}
                {currentUnit.isFreePreview && showCouponAlert && (
                  <div className="absolute top-4 left-1/2 transform -translate-x-1/2 z-10 w-full max-w-3xl px-4">
                    <Alert className="bg-yellow-50 dark:bg-yellow-900/20 border-yellow-200 dark:border-yellow-800">
                      <AlertDescription className="flex items-center justify-between text-yellow-800 dark:text-yellow-200">
                        <span>將此體驗課程的全部影片看完就可以獲得 3000 元課程折價券！</span>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="h-6 w-6 ml-2 hover:bg-yellow-100 dark:hover:bg-yellow-800"
                          onClick={() => setShowCouponAlert(false)}
                        >
                          <X className="h-4 w-4" />
                        </Button>
                      </AlertDescription>
                    </Alert>
                  </div>
                )}

                {/* YouTube 播放器（使用 ReactPlayer，添加穩定的 key） */}
                <div
                  key={`player-${currentUnit.unitId}`}
                  ref={playerContainerRef}
                  className="relative w-full flex-1 bg-black"
                >
                  {currentUnit.videoUrl ? (
                    <>
                      {/* 調試信息 */}
                      <div className="absolute top-4 left-4 z-50 bg-blue-600 text-white p-4 rounded text-xs max-w-md">
                        <p className="font-bold">調試信息：</p>
                        <p>原始 URL: {currentUnit.videoUrl}</p>
                        <p>Video ID: {getYouTubeVideoId(currentUnit.videoUrl)}</p>
                        <p>Key: player-{currentUnit.unitId}</p>
                      </div>

                      {/* ReactPlayer with stable key */}
                      <ReactPlayer
                        ref={playerRef}
                        url={currentUnit.videoUrl}
                        playing={playing}
                        controls={true}
                        volume={volume}
                        muted={muted}
                        width="100%"
                        height="100%"
                        style={{ position: 'absolute', top: 0, left: 0 }}
                        config={{
                          youtube: {
                            playerVars: {
                              modestbranding: 1,
                              rel: 0,
                            }
                          }
                        }}
                        onPlay={() => console.log('onPlay')}
                        onPause={() => console.log('onPause')}
                      />
                    </>
                  ) : (
                    <div className="text-white text-center flex items-center justify-center h-full">
                      <div>
                        <p>找不到影片 URL</p>
                        <p className="text-sm text-gray-400 mt-2">請確認課程單元設定</p>
                      </div>
                    </div>
                  )}
                </div>

                {/* 交付按鈕（固定在右下角） */}
                <div className="absolute bottom-8 right-8 z-20">
                  <Button
                    onClick={handleCompleteUnit}
                    disabled={completing || currentUnit.isCompleted}
                    size="lg"
                    className="bg-yellow-600 hover:bg-yellow-700 text-black shadow-lg"
                    data-testid="complete-unit-button"
                  >
                    {currentUnit.isCompleted ? '已完成' : completing ? '處理中...' : '標記為完成'}
                  </Button>
                </div>

                {currentUnit.isCompleted && (
                  <div className="absolute bottom-8 left-1/2 transform -translate-x-1/2 z-10">
                    <div className="bg-green-600 text-white px-6 py-3 rounded-lg shadow-lg">
                      ✅ 您已完成此單元並獲得 {currentUnit.xpReward} XP
                    </div>
                  </div>
                )}
              </>
            ) : (
              // 無權限：顯示鎖定提示
              <div className="flex items-center justify-center h-full bg-background">
                <Card className="p-8 text-center max-w-md">
                  <Lock className="h-16 w-16 mx-auto mb-4 text-muted-foreground" />
                  <h2 className="text-2xl font-bold mb-2">
                    您無法觀看「{currentUnit.title}」
                  </h2>
                  <p className="text-muted-foreground mb-6">
                    這是課程「{currentUnit.courseTitle}」購買後才能享有的內容。
                  </p>
                  <Button asChild size="lg" className="bg-yellow-600 hover:bg-yellow-700 text-black">
                    <a href={`/courses/${courseCode}`}>
                      前往課程頁購買
                    </a>
                  </Button>
                </Card>
              </div>
            )}
          </div>
        </main>
      </div>

      {/* 登入提示 Dialog */}
      <AlertDialog open={showLoginDialog} onOpenChange={setShowLoginDialog}>
        <AlertDialogContent>
          <AlertDialogHeader>
            <AlertDialogTitle>需要先登入</AlertDialogTitle>
            <AlertDialogDescription>
              請先登入帳號才能試看課程或觀看單元內容。
            </AlertDialogDescription>
          </AlertDialogHeader>
          <AlertDialogFooter>
            <AlertDialogCancel>稍後再說</AlertDialogCancel>
            <AlertDialogAction
              onClick={() => {
                const currentPath = window.location.pathname
                router.push(`/login?redirectTo=${encodeURIComponent(currentPath)}`)
              }}
            >
              前往登入
            </AlertDialogAction>
          </AlertDialogFooter>
        </AlertDialogContent>
      </AlertDialog>
    </>
  )
}
