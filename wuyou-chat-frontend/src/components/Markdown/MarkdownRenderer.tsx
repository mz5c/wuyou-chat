import ReactMarkdown from 'react-markdown';
import { Prism as SyntaxHighlighter } from 'react-syntax-highlighter';
import { oneLight } from 'react-syntax-highlighter/dist/esm/styles/prism';
import type { Components } from 'react-markdown';

interface Props {
  content: string;
}

export function MarkdownRenderer({ content }: Props) {
  const components: Components = {
    code({ className, children, ...props }) {
      const match = /language-(\w+)/.exec(className || '');
      const codeStr = String(children).replace(/\n$/, '');
      if (match) {
        return (
          <SyntaxHighlighter
            style={oneLight}
            language={match[1]}
            PreTag="div"
          >
            {codeStr}
          </SyntaxHighlighter>
        );
      }
      return <code className={className} {...props}>{children}</code>;
    },
  };

  return (
    <div className="markdown-body">
      <ReactMarkdown components={components}>
        {content}
      </ReactMarkdown>
    </div>
  );
}
