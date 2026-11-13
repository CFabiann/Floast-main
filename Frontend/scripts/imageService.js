// ============================================================
//  FLOAST — imageService.js
//  Compresión en cliente (WebP) y subida directa a Cloudinary
// ============================================================

// Configura aquí tus datos gratuitos de Cloudinary (Settings -> Upload -> Upload Presets -> Unsigned)
const CLOUDINARY_CLOUD_NAME = "awuroa86";
const CLOUDINARY_UPLOAD_PRESET = "floast_unsigned"; // créalo en Cloudinary como Unsigned

export const MAX_PHOTOS = 5;

/**
 * Comprime y redimensiona una imagen usando HTML5 Canvas a formato WebP
 */
export function compressImage(file, maxWidth = 1200, quality = 0.8) {
  return new Promise((resolve, reject) => {
    const reader = new FileReader();
    reader.readAsDataURL(file);

    reader.onload = (event) => {
      const img = new Image();
      img.src = event.target.result;

      img.onload = () => {
        let width = img.width;
        let height = img.height;

        if (width > maxWidth) {
          height = Math.round((height * maxWidth) / width);
          width = maxWidth;
        }

        const canvas = document.createElement("canvas");
        canvas.width = width;
        canvas.height = height;

        const ctx = canvas.getContext("2d");
        ctx.drawImage(img, 0, 0, width, height);

        const dataUrl = canvas.toDataURL("image/webp", quality);

        canvas.toBlob(
          (blob) => resolve({ blob, dataUrl }),
          "image/webp",
          quality
        );
      };

      img.onerror = (err) => reject(err);
    };

    reader.onerror = (err) => reject(err);
  });
}

/**
 * Sube una imagen a Cloudinary y devuelve la URL pública (https://...)
 * Si aún no hay credenciales configuradas, devuelve la imagen WebP optimizada
 * para poder probar todo el flujo en el navegador.
 */
export async function uploadImageToCloud(file) {
  const { blob } = await compressImage(file);

  // Si ya configuraste Cloudinary, sube el archivo comprimido
  if (CLOUDINARY_CLOUD_NAME && CLOUDINARY_UPLOAD_PRESET) {
    const formData = new FormData();
    formData.append("file", blob, file.name.replace(/\.[^/.]+$/, "") + ".webp");
    formData.append("upload_preset", CLOUDINARY_UPLOAD_PRESET);

    const response = await fetch(
      `https://api.cloudinary.com/v1_1/${CLOUDINARY_CLOUD_NAME}/image/upload`,
      {
        method: "POST",
        body: formData
      }
    );

    if (!response.ok) {
      throw new Error("Error al subir la imagen a Cloudinary (revisa cloud name y upload preset unsigned)");
    }

    const data = await response.json();
    return data.secure_url;
  }

  // Sin credenciales no se puede guardar: el backend solo acepta URLs de Cloudinary
  // (un data:base64 excedería el límite de Firestore y es rechazado con 400).
  throw new Error(
    "Cloudinary no está configurado: completa CLOUDINARY_CLOUD_NAME y CLOUDINARY_UPLOAD_PRESET en imageService.js"
  );
}