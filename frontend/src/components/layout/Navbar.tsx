import { Link, useLocation } from 'react-router-dom';
import { cn } from '../../lib/utils';

const links = [
  { to: '/',           label: 'Search'    },
  { to: '/stream',     label: 'Live'      },
  { to: '/dashboard',  label: 'Dashboard' },
  { to: '/alerts',     label: 'Alerts'    },
];

export function Navbar() {
  const { pathname } = useLocation();

  return (
    <nav className="border-b border-gray-200 bg-white">
      <div className="max-w-7xl mx-auto px-4 h-14 flex items-center justify-between">
        <div className="flex items-center gap-6">
          <span className="font-bold text-lg tracking-tight">
            Log<span className="text-blue-600">Lens</span>
          </span>
          <div className="flex items-center gap-1">
            {links.map(link => (
              <Link
                key={link.to}
                to={link.to}
                className={cn(
                  'px-3 py-1.5 rounded-md text-sm font-medium transition-colors',
                  pathname === link.to
                    ? 'bg-blue-50 text-blue-700'
                    : 'text-gray-600 hover:text-gray-900 hover:bg-gray-50'
                )}
              >
                {link.label}
              </Link>
            ))}
          </div>
        </div>
      </div>
    </nav>
  );
}
