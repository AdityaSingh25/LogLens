import { render, screen, act } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi, beforeEach } from 'vitest';
import { SearchBar } from './SearchBar';

describe('SearchBar', () => {
  beforeEach(() => {
    vi.useFakeTimers();
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it('renders placeholder text', () => {
    render(<SearchBar onSearch={vi.fn()} />);
    expect(screen.getByPlaceholderText(/search logs in plain english/i)).toBeTruthy();
  });

  it('does not call onSearch for empty input', async () => {
    const onSearch = vi.fn();
    render(<SearchBar onSearch={onSearch} />);
    const input = screen.getByRole('textbox');
    await userEvent.type(input, '   ');
    await act(() => vi.advanceTimersByTime(400));
    expect(onSearch).not.toHaveBeenCalled();
  });

  it('debounces and calls onSearch after 300ms', async () => {
    const onSearch = vi.fn();
    render(<SearchBar onSearch={onSearch} />);
    const input = screen.getByRole('textbox');
    await userEvent.type(input, 'payment errors');
    expect(onSearch).not.toHaveBeenCalled();
    await act(() => vi.advanceTimersByTime(350));
    expect(onSearch).toHaveBeenCalledWith('payment errors');
  });
});
