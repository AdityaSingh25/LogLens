import OpenAI from 'openai';
import { config } from '../config/config';

const openai = new OpenAI({ apiKey: config.OPENAI_API_KEY });

/**
 * Generates embeddings for a batch of texts.
 * Returns null on failure — embedding is best-effort and must never block ingestion.
 */
export async function embedTexts(texts: string[]): Promise<number[][] | null> {
  if (texts.length === 0) return [];
  try {
    const response = await openai.embeddings.create({
      model: config.EMBEDDING_MODEL,
      input: texts,
    });
    return response.data
      .sort((a, b) => a.index - b.index)
      .map(item => item.embedding);
  } catch (err) {
    console.error('[openaiClient] Embedding generation failed:', err);
    return null;
  }
}
