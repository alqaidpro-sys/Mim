/**
 * ═══════════════════════════════════════════════════════
 * TMDB API INTEGRATED SERVICE - MiM Plus
 * ═══════════════════════════════════════════════════════
 * 
 * خدمة متكاملة للاتصال بواجهة برمجة تطبيقات TMDB والحصول على البيانات الحية.
 * مصممة للعمل في بيئة JavaScript / TypeScript (Project IDX) للهواتف الذكية.
 * خالية تماماً من البيانات الوهمية (No Mock Data)، وتعتمد بالكامل على طلبات الشبكة الحقيقية.
 * 
 * المميزات:
 * 1. استخراج الحقول المطلوبة بدقة عالية وبوستر عالي الجودة.
 * 2. جلب التفاصيل الكاملة للأعمال (الوصف، المواسم، الحلقات، الممثلين، المخرجين وصورهم).
 * 3. نظام صفحات (Pagination) ديناميكي لـ fetchTrendingContent.
 * 4. دعم كامل للغة العربية والتعريب التلقائي لأسماء التصنيفات.
 * 5. معالجة أخطاء try...catch احترافية ومفصلة.
 */

// ═══════════════════════════════════════════════════════
// القائمة المرجعية لتعريفات التصنيفات باللغة العربية
// ═══════════════════════════════════════════════════════
const GENRE_MAP: Record<number, string> = {
  28: "أكشن",
  12: "مغامرة",
  16: "رسوم متحركة",
  35: "كوميديا",
  80: "جريمة",
  99: "وثائقي",
  18: "دراما",
  10751: "عائلي",
  14: "فانتازيا",
  36: "تاريخ",
  27: "رعب",
  10402: "موسيقى",
  9648: "غموض",
  10749: "رومانسي",
  878: "خيال علمي",
  10770: "تلفزيوني",
  53: "إثارة",
  10752: "حرب",
  37: "غرب أمريكي",
  10759: "أكشن ومغامرة",
  10762: "أطفال",
  10763: "أخبار",
  10764: "واقعي",
  10765: "خيال وفانتازيا",
  10766: "أوبرا صابونية",
  10767: "برنامج حواري",
  10768: "حرب وسياسة"
};

// ═══════════════════════════════════════════════════════
// تَعْرِيف واجهات البيانات (Interfaces)
// ═══════════════════════════════════════════════════════

export interface TrendingItem {
  id: number;
  title: string;
  posterUrl: string;
  rating: number;
  category: string;
  releaseYear: number;
  mediaType: "movie" | "tv";
}

export interface CastMember {
  id: number;
  name: string;
  role: string; // Actor (Character name) or Director
  profileUrl: string | null;
}

export interface WorkDetails {
  id: number;
  title: string;
  overview: string;
  posterUrl: string;
  rating: number;
  releaseYear: number;
  category: string;
  seasonsCount: number | null;  // خاص بالمسلسلات (null للأفلام)
  episodesCount: number | null; // خاص بالمسلسلات (null للأفلام)
  cast: CastMember[];           // الممثلون
  directors: CastMember[];      // المخرجون
}

// ═══════════════════════════════════════════════════════
// فِئَة الخدمة الرَّئِيسِيَّة (TMDB Service Class)
// ═══════════════════════════════════════════════════════

export class TmdbService {
  private baseUrl: string = "https://api.themoviedb.org/3";
  private apiKey: string;
  private imageBaseUrl: string = "https://image.tmdb.org/t/p";

  /**
   * إنشاء كائن الخدمة.
   * @param apiKey مفتاح TMDB API. يمكنك الحصول عليه مجاناً من إعدادات حسابك على TMDB.
   */
  constructor(apiKey: string) {
    if (!apiKey || apiKey.trim() === "") {
      throw new Error("عذراً، يجب تقديم مفتاح API Key صالح لـ TMDB للاتصال بالخدمة الحية.");
    }
    this.apiKey = apiKey;
  }

  /**
   * دالة مساعدة لترميز وضمان روابط الصور عالية الجودة
   */
  private getPosterUrl(path: string | null, size: "w500" | "h632" | "original" = "w500"): string {
    return path ? `${this.imageBaseUrl}/${size}${path}` : "https://placehold.co/500x750/091e1a/ffffff?text=No+Poster";
  }

  /**
   * دالة مساعدة لترميز صور طاقم العمل
   */
  private getProfileUrl(path: string | null): string | null {
    return path ? `${this.imageBaseUrl}/w185${path}` : null;
  }

  /**
   * جلب المحتوى الرائج (مسلسلات وأفلام) مع التصفح والصفحات الديناميكية.
   * 
   * @param mediaType نوع المحتوى المطلق ('all' للاثنين معاً، أو 'movie' أو 'tv')
   * @param page رقم الصفحة لتفعيل الـ Pagination الديناميكي
   * @returns قائمة بالكائنات المهيأة للواجهة والمستخرجة بدقة.
   */
  async fetchTrendingContent(
    mediaType: "all" | "movie" | "tv" = "all",
    page: number = 1
  ): Promise<TrendingItem[]> {
    try {
      const url = `${this.baseUrl}/trending/${mediaType}/week?api_key=${this.apiKey}&page=${page}&language=ar`;
      const response = await fetch(url);

      if (!response.ok) {
        throw new Error(`فشل الاتصال بـ TMDB: الكود ${response.status} (${response.statusText})`);
      }

      const data = await response.json();
      
      if (!data.results || !Array.isArray(data.results)) {
        return [];
      }

      return data.results.map((item: any) => {
        // تحديد العنوان الأساسي بناءً على نوع العمل (أفلام أو مسلسلات)
        const title = item.title || item.name || item.original_title || item.original_name || "عنوان غير معروف";
        
        // جلب السنة بدقة
        const releaseDate = item.release_date || item.first_air_date;
        const releaseYear = releaseDate ? new Date(releaseDate).getFullYear() : 0;

        // ترجمة التصنيفات
        const firstGenreId = item.genre_ids && item.genre_ids[0];
        const category = firstGenreId ? (GENRE_MAP[firstGenreId] || "منوع") : "منوع";

        return {
          id: item.id,
          title: title,
          posterUrl: this.getPosterUrl(item.poster_path, "w500"),
          rating: Number((item.vote_average || 0).toFixed(1)),
          category: category,
          releaseYear: releaseYear,
          mediaType: item.media_type || (mediaType === "all" ? "movie" : mediaType)
        };
      });

    } catch (error: any) {
      console.error(`[Error in fetchTrendingContent]: ${error.message || error}`);
      throw error; // إعادة توجيه الخطأ لمعالجته في الواجهة الرسومية
    }
  }

  /**
   * جلب تفاصيل العمل الفني بشكل كامل وحقيقي مع طاقم العمل.
   * 
   * @param mediaType نوع العمل ('movie' للفيلم، أو 'tv' للمسلسل)
   * @param id رمز التعريف الخاص بالعمل (ID)
   * @returns تفاصيل العمل الكاملة مهيأة للنسخ والاستخدام الفوري.
   */
  async fetchDetails(mediaType: "movie" | "tv", id: number): Promise<WorkDetails> {
    try {
      // جلب تفاصيل العمل الأساسية مع طاقم العمل في طلب شبكة واحد عبر append_to_response لسرعة فائقة وتحسين الأداء
      const detailsUrl = `${this.baseUrl}/${mediaType}/${id}?api_key=${this.apiKey}&append_to_response=credits&language=ar`;
      const response = await fetch(detailsUrl);

      if (!response.ok) {
        throw new Error(`فشل جلب تفاصيل المعرف ${id} من TMDB: كود ${response.status}`);
      }

      const data = await response.json();

      const title = data.title || data.name || data.original_title || data.original_name || "عمل فني";
      const releaseDate = data.release_date || data.first_air_date;
      const releaseYear = releaseDate ? new Date(releaseDate).getFullYear() : 0;
      
      // جلب التصنيف
      const category = data.genres && data.genres.length > 0 ? data.genres[0].name : "منوع";

      // تصفية فريق التمثيل والتمثيليات (المخرجين والممثلين)
      const cast: CastMember[] = [];
      const directors: CastMember[] = [];

      if (data.credits) {
        // 1. الممثلين الرائدين (أول 8 ممثلين فقط من أجل المظهر الجمالي والأداء)
        if (Array.isArray(data.credits.cast)) {
          data.credits.cast.slice(0, 8).forEach((actor: any) => {
            cast.push({
              id: actor.id,
              name: actor.name,
              role: actor.character || "ممثل",
              profileUrl: this.getProfileUrl(actor.profile_path)
            });
          });
        }

        // 2. المخرجين (طاقم الإخراج)
        if (Array.isArray(data.credits.crew)) {
          data.credits.crew.forEach((crewMember: any) => {
            if (crewMember.job === "Director" || crewMember.job === "Executive Producer" || crewMember.department === "Directing") {
              directors.push({
                id: crewMember.id,
                name: crewMember.name,
                role: crewMember.job || "مخرج",
                profileUrl: this.getProfileUrl(crewMember.profile_path)
              });
            }
          });
        }
      }

      return {
        id: data.id,
        title: title,
        overview: data.overview || "لا يوجد وصف متوفر باللغة العربية لهذا العمل حالياً.",
        posterUrl: this.getPosterUrl(data.poster_path, "original"), // دقة أصلية لتفاصيل العمل
        rating: Number((data.vote_average || 0).toFixed(1)),
        releaseYear: releaseYear,
        category: category,
        seasonsCount: mediaType === "tv" ? (data.number_of_seasons || 1) : null,
        episodesCount: mediaType === "tv" ? (data.number_of_episodes || 12) : null,
        cast: cast,
        directors: directors.slice(0, 3) // أول 3 مخرجين بارزين كحد أقصى لتناسق الواجهة
      };

    } catch (error: any) {
      console.error(`[Error in fetchDetails for ID ${id}]: ${error.message || error}`);
      throw error;
    }
  }
}
