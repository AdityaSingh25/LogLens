export const KafkaTopics = {
  RAW_LOGS: 'raw-logs',
  PARSED_LOGS: 'parsed-logs',
  EMBEDDING_RESULTS: 'embedding-results',
  ALERT_EVENTS: 'alert-events',
} as const;

export type KafkaTopic = typeof KafkaTopics[keyof typeof KafkaTopics];

export const deadLetter = (topic: string): string => `${topic}.DLT`;
