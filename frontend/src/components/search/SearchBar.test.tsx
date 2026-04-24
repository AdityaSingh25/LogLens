import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { describe, it, expect, vi } from 'vitest';
import { SearchBar } from './SearchBar';

describe('SearchBar', () => {
  it('renders placeholder text', () => {
    render(<SearchBar onSearch={vi.fn()} />);
    expect(screen.getByPlaceholderText(/search logs in plain english/i)).toBeTruthy();
  });

  it('does not call onSearch for whitespace-only input', async () => {
    const user = userEvent.setup();
    const onSearch = vi.fn();
    render(<SearchBar onSearch={onSearch} />);
    await user.type(screen.getByRole('textbox'), '   ');
    // wait beyond debounce window
    await new Promise(r => setTimeout(r, 400));
    expect(onSearch).not.toHaveBeenCalled();
  });

  it('calls onSearch with typed value after debounce', async () => {
    const user = userEvent.setup();
    const onSearch = vi.fn();
    render(<SearchBar onSearch={onSearch} />);
    await user.type(screen.getByRole('textbox'), 'payment errors');
    await waitFor(() => expect(onSearch).toHaveBeenCalledWith('payment errors'), { timeout: 1000 });
  });
});
