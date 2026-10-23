// ============================================================
//  FLOAST — reviewService.js
//  Capa de datos para reseñas de inquilinos verificados.
// ============================================================

const API_BASE = "http://localhost:8080/api/properties";

// Datos de prueba locales (se usan si el backend aún no responde)
const MOCK_REVIEWS = {
  prop_2: [
    {
      id: "rev_1",
      propId: "prop_2",
      userName: "Laura Pérez Díaz",
      userEmail: "laura.pd@gmail.com",
      unitLabel: "Habitación 1",
      rating: 5,
      tags: ["Agua constante", "Internet estable", "Zona segura"],
      comment: "Excelente lugar para vivir, los servicios nunca fallan y el ambiente es muy respetuoso.",
      createdAt: "2026-09-15"
    }
  ]
};

/**
 * Obtiene las reseñas paginadas de una propiedad
 */
export async function getReviews(propId, page = 1, size = 5) {
  try {
    const url = new URL(`${API_BASE}/${propId}/reviews`);
    url.searchParams.append("page", page);
    url.searchParams.append("size", size);

    const response = await fetch(url);
    if (!response.ok) throw new Error("Endpoint de reseñas no disponible aún");

    // Espera { content: [...], totalPages: X, averageRating: 4.8, totalReviews: 12 }
    return await response.json();
  } catch (error) {
    console.warn("Usando reseñas locales de respaldo:", error.message);
    const list = MOCK_REVIEWS[propId] || [];
    const avg = list.length
      ? (list.reduce((acc, r) => acc + r.rating, 0) / list.length).toFixed(1)
      : 0;
    return {
      content: list,
      totalPages: 1,
      averageRating: Number(avg),
      totalReviews: list.length
    };
  }
}

/**
 * Guarda una nueva reseña de un inquilino verificado
 */
export async function addReview(propId, reviewData) {
  try {
    const response = await fetch(`${API_BASE}/${propId}/reviews`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(reviewData)
    });

    if (!response.ok) {
      const errText = await response.text();
      throw new Error(errText || "No se pudo guardar la reseña.");
    }

    return await response.json();
  } catch (error) {
    console.warn("Guardando reseña en modo local de prueba:", error.message);
    if (!MOCK_REVIEWS[propId]) MOCK_REVIEWS[propId] = [];
    const newRev = {
      id: `rev_${Date.now()}`,
      propId,
      createdAt: new Date().toISOString().split("T")[0],
      ...reviewData
    };
    MOCK_REVIEWS[propId].unshift(newRev);
    return newRev;
  }
}