import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { getAlertRules, createAlertRule, toggleAlertRule } from '../api/alerts';
import type { AlertRule } from '../types';

export function useAlertRules() {
  return useQuery({
    queryKey: ['alerts'],
    queryFn: getAlertRules,
    retry: 1,
  });
}

export function useCreateAlertRule() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: (rule: Omit<AlertRule, 'ruleId' | 'tenantId' | 'createdAt'>) =>
      createAlertRule(rule),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['alerts'] }),
  });
}

export function useToggleAlertRule() {
  const queryClient = useQueryClient();
  return useMutation({
    mutationFn: ({ ruleId, enabled }: { ruleId: string; enabled: boolean }) =>
      toggleAlertRule(ruleId, enabled),
    onSuccess: () => queryClient.invalidateQueries({ queryKey: ['alerts'] }),
  });
}
